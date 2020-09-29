package deltix.samples.timebase.process.pub;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 *
 */
public class ConfigurableMessageProcessorFactory implements MessageProcessorFactory {
    private final Map <String, Object>  properties = 
        new TreeMap <String, Object> ();
    
    private ClassLoader                 classLoader = 
        ConfigurableMessageProcessorFactory.class.getClassLoader ();
    
    private String                      processorClassName = null;
    
    public ClassLoader                  getClassLoader () {
        return classLoader;
    }

    public void                         setClassLoader (ClassLoader classLoader) {
        this.classLoader = classLoader;
    }    

    public void                         setProcessorClass (
        Class <? extends MessageProcessor> cls
    )        
    {
        processorClassName = cls.getName ();
        classLoader = cls.getClassLoader ();
    }
    
    public void                         clearProperties () {
        properties.clear ();
    }
    
    public void                         setProperty (String name, Object value) {
        properties.put (name, value);
    }
    
    @Override
    public MessageProcessor             newProcessor () {
        if (processorClassName == null)
            throw new IllegalStateException (
                "Processor class name is not set."
            );
        
        MessageProcessor    mp;
        
        try {
            Class <?>           cls = classLoader.loadClass (processorClassName);
            Method []           methods = cls.getDeclaredMethods ();
            
            mp = (MessageProcessor) cls.newInstance ();

            for (Map.Entry <String, Object> e : properties.entrySet ()) {
                String          key = e.getKey ();

                String          methodName = 
                    "set" + Character.toUpperCase (key.charAt (0)) + key.substring (1);

                Method          method = null;
                
                for (Method m : methods) {
                    if (m.getName ().equals (methodName) && 
                        m.getParameterTypes ().length == 1) 
                    {
                        method = m;
                        break;
                    }
                }
                        
                if (method == null) 
                    throw new RuntimeException (
                        "Method '" + methodName + "' not found in class " +
                        processorClassName
                    );
                
                Class <?>       argType = method.getParameterTypes () [0];
                String          textValue = e.getValue ().toString ();
                Object          value;
                
                if (CharSequence.class.isAssignableFrom (argType))
                    value = textValue;
                else if (argType == byte.class)
                    value = new Byte (textValue);
                else if (argType == short.class)
                    value = new Short (textValue);
                else if (argType == int.class)
                    value = new Integer (textValue);
                else if (argType == long.class)
                    value = new Long (textValue);
                else if (argType == float.class)
                    value = new Float (textValue);
                else if (argType == double.class)
                    value = new Double (textValue);
                else if (argType == char.class)
                    value = new Character (textValue.charAt (0));
                else
                    value = argType.getConstructor (String.class).newInstance (textValue);
                
                method.invoke (mp, value);
            }
        } catch (ClassNotFoundException x) {
            throw new RuntimeException (x);
        } catch (IllegalAccessException x) {
            throw new RuntimeException (x);
        } catch (InvocationTargetException x) {
            throw new RuntimeException (x);
        } catch (NoSuchMethodException x) {
            throw new RuntimeException (x);
        } catch (InstantiationException x) {
            throw new RuntimeException (x);
        }
        
        return (mp);        
    }        
}
