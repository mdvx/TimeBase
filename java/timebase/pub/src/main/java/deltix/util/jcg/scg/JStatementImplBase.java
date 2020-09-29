package deltix.util.jcg.scg;

import deltix.util.jcg.*;

/**
 *
 */
abstract class JStatementImplBase implements JStatement, JCompStmtElem {
    final JContextImpl                  context;
    
    public JStatementImplBase (JContextImpl context) {
        this.context = context;
    }
}
