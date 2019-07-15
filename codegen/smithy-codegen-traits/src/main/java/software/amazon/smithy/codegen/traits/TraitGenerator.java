package software.amazon.smithy.codegen.traits;

import java.util.Locale;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.shapes.ShapeVisitor;
import software.amazon.smithy.model.shapes.StringShape;
import software.amazon.smithy.model.shapes.StructureShape;
import software.amazon.smithy.model.traits.TraitDefinition;
import software.amazon.smithy.utils.StringUtils;

/**
 * Converts a trait to code.
 */
interface TraitGenerator {

    /**
     * @return Gets the trait definition.
     */
    TraitDefinition getTraitDefinition();

    /**
     * @return Gets the formatted code.
     */
    String getCode();

    /**
     * @return Gets the Java package name of the trait.
     */
    default String getPackageName() {
        return getTraitDefinition().getNamespace().toLowerCase(Locale.ENGLISH);
    }

    /**
     * @return Gets the Java class name of the trait.
     */
    default String getClassName() {
        String name = getTraitDefinition().getName();

        if (!name.endsWith("Trait")) {
            name += "Trait";
        }

        return StringUtils.capitalize(name);
    }

    /**
     * @return Gets the Java class name of the trait provider.
     */
    default String getProviderClassName() {
        return getClassName() + "$Provider";
    }

    /**
     * Factory method for creating trait generators.
     *
     * @param model Model to create the trait from.
     * @param traitName Name of the trait convert to code.
     *
     * @return Returns the create {@link TraitGenerator}.
     */
    static TraitGenerator create(Model model, String traitName) {
        TraitDefinition definition = model.getTraitDefinition(traitName)
                .orElseThrow(() -> new IllegalArgumentException("Trait not found in model: " + traitName));

        if (!definition.getShape().isPresent()) {
            return new AnnotationTraitGenerator(definition);
        }

        ShapeId target = definition.getShape().get();
        Shape targetShape = model.getShapeIndex().getShape(target)
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "Trait `%s` target shape not found in model: %s", traitName, target)));

        return targetShape.accept(new ShapeVisitor.Default<TraitGenerator>() {
            @Override
            protected TraitGenerator getDefault(Shape shape) {
                throw new UnsupportedOperationException("TODO");
            }

            @Override
            public TraitGenerator stringShape(StringShape shape) {
                return new StringTraitGenerator(definition, shape);
            }

            @Override
            public TraitGenerator structureShape(StructureShape shape) {
                return new StructureTraitGenerator(model, definition, shape);
            }
        });
    }
}
