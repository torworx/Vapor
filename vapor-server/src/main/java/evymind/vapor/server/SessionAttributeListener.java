package evymind.vapor.server;

import java.util.EventListener;

public interface SessionAttributeListener extends EventListener {

    /**
     * Receives notification that an attribute has been added to a
     * session.
     *
     * @param event the SessionBindingEvent containing the session
     * and the name and value of the attribute that was added
     */
    public void attributeAdded(SessionBindingEvent event);

    /**
     * Receives notification that an attribute has been removed from a
     * session.
     *
     * @param event the SessionBindingEvent containing the session
     * and the name and value of the attribute that was removed
     */
    public void attributeRemoved(SessionBindingEvent event);

    /**
     * Receives notification that an attribute has been replaced in a
     * session.
     *
     * @param event the SessionBindingEvent containing the session
     * and the name and (old) value of the attribute that was replaced
     */
    public void attributeReplaced(SessionBindingEvent event);
}
