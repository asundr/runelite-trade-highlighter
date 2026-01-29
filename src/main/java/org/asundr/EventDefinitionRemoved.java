package org.asundr;

public class EventDefinitionRemoved
{
    private final HighlightDefinition definition;
    EventDefinitionRemoved(final HighlightDefinition definition) { this.definition = definition; }
    public HighlightDefinition getDefinition() { return definition; }
}
