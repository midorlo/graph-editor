package de.tesis.dynaware.grapheditor.demo.customskins;

import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.tesis.dynaware.grapheditor.Commands;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.GraphEditorContainer;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;
import de.tesis.dynaware.grapheditor.model.GraphPackage;

public class DefaultSkinManager {

    protected static final int NODE_INITIAL_X = 19;
    protected static final int NODE_INITIAL_Y = 19;

    protected static final String INPUT_CONNECTOR_TYPE = "input";
    protected static final String OUTPUT_CONNECTOR_TYPE = "output";

    protected static final int MAX_CONNECTOR_COUNT = 5;

    protected final GraphEditor graphEditor;
    protected final GraphEditorContainer graphEditorContainer;

    public DefaultSkinManager(final GraphEditor graphEditor, final GraphEditorContainer graphEditorContainer) {

        this.graphEditor = graphEditor;
        this.graphEditorContainer = graphEditorContainer;
    }

    public void addNode(final double currentZoomFactor) {

        final double windowXOffset = graphEditorContainer.windowXProperty().get() / currentZoomFactor;
        final double windowYOffset = graphEditorContainer.windowYProperty().get() / currentZoomFactor;

        final GNode node = GraphFactory.eINSTANCE.createGNode();
        node.setY(NODE_INITIAL_Y + windowYOffset);

        final GConnector output = GraphFactory.eINSTANCE.createGConnector();
        node.getConnectors().add(output);

        final GConnector input = GraphFactory.eINSTANCE.createGConnector();
        node.getConnectors().add(input);

        node.setX(NODE_INITIAL_X + windowXOffset);

        input.setType(INPUT_CONNECTOR_TYPE);
        output.setType(OUTPUT_CONNECTOR_TYPE);

        Commands.addNode(graphEditor.getModel(), node);
    }

    public void addInputConnector() {
        addConnector(INPUT_CONNECTOR_TYPE);
    }

    public void addOutputConnector() {
        addConnector(OUTPUT_CONNECTOR_TYPE);
    }

    /**
     * Adds a connector of the given type to all nodes that are currently selected.
     *
     * @param type the type for the new connector
     */
    protected void addConnector(final String type) {

        final GModel model = graphEditor.getModel();
        final SkinLookup skinLookup = graphEditor.getSkinLookup();
        final CompoundCommand command = new CompoundCommand();
        final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(model);

        for (final GNode node : model.getNodes()) {

            if (skinLookup.lookupNode(node).isSelected()) {
                if (countConnectors(node, type) < MAX_CONNECTOR_COUNT) {

                    final GConnector connector = GraphFactory.eINSTANCE.createGConnector();
                    connector.setType(type);

                    final EReference connectors = GraphPackage.Literals.GCONNECTABLE__CONNECTORS;
                    command.append(AddCommand.create(editingDomain, node, connectors, connector));
                }
            }
        }

        if (command.canExecute()) {
            graphEditor.getSelectionManager().backup();
            editingDomain.getCommandStack().execute(command);
            graphEditor.getSelectionManager().restore();
        }
    }

    /**
     * Counts the number of connectors the given node currently has of the given type.
     *
     * @param node a {@link GNode} instance
     * @param type a type String
     * @return the number of connectors this node has of the given type
     */
    private int countConnectors(final GNode node, final String type) {

        int count = 0;

        for (final GConnector connector : node.getConnectors()) {
            if (connector.getType() == null && type == null || connector.getType() != null
                    && connector.getType().equals(type)) {
                count++;
            }
        }

        return count;
    }
}
