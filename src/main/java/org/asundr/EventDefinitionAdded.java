package org.asundr;

public class EventDefinitionAdded
{
    private final HighlightDefinition definition;
    EventDefinitionAdded(final HighlightDefinition definition) { this.definition = definition; }
    public HighlightDefinition getDefinition() { return definition; }
}

