package deltix.util.jcg.scg;

import deltix.util.jcg.JMember;
import java.io.IOException;

/**
 *
 */
interface JMemberIntf extends JMember {
    abstract void           printDeclaration (SourceCodePrinter out)
        throws IOException;
}
