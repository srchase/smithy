package software.amazon.smithy.codegen.traits;

import java.util.Optional;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.MemberShape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.shapes.StructureShape;
import software.amazon.smithy.model.traits.AbstractTrait;
import software.amazon.smithy.model.traits.AbstractTraitBuilder;
import software.amazon.smithy.model.traits.TraitDefinition;
import software.amazon.smithy.utils.SmithyBuilder;
import software.amazon.smithy.utils.StringUtils;
import software.amazon.smithy.utils.ToSmithyBuilder;

/**
 * TODO: This should really augment some generic structure generator.
 * TODO: Member return values are hardcoded to String.
 * TODO: Use the TraitSymbolProvider.
 * TODO: Generate the createNode and Provider (from node) methods.
 * TODO: Add javadocs for all relevant methods.
 */
class StructureTraitGenerator implements TraitGenerator {
    private final Model model;
    private final TraitDefinition trait;
    private final StructureShape shape;
    private final boolean noRequiredMembers;

    StructureTraitGenerator(Model model, TraitDefinition trait, StructureShape shape) {
        this.model = model;
        this.trait = trait;
        this.shape = shape;
        noRequiredMembers = shape.getAllMembers().values().stream().noneMatch(MemberShape::isRequired);
    }

    @Override
    public TraitDefinition getTraitDefinition() {
        return trait;
    }

    @Override
    public String getCode() {
        String className = getClassName();
        JavaCodeWriter writer = new JavaCodeWriter(getPackageName());

        getTraitDefinition().getDocumentation().ifPresent(docs -> {
            String formattedDocs = StringUtils.wrap(docs, 74);
            writer.javadoc(() -> writer.write(formattedDocs));
        });

        writer.openBlock("public final class $1L extends $2T implements $3T<$1L> {",
                         className, AbstractTrait.class, ToSmithyBuilder.class)
                .write("public static final String NAME = $S;", getTraitDefinition().getFullyQualifiedName())
                .write();

        addMembers(writer);

        // Add empty constructors if no members are required.
        if (noRequiredMembers) {
            addEmptyConstructors(writer);
        }

        addBuilderConstructor(writer);
        addGetters(writer);
        addToNode(writer);
        addToBuilder(writer);
        addBuilder(writer);
        addProvider(writer);

        writer.closeBlock("}");

        return writer.toString();
    }

    private void addEmptyConstructors(JavaCodeWriter writer) {
        writer
                .openBlock("public $L($T sourceLocation) {", getClassName(), SourceLocation.class)
                    .write("this(builder().sourceLocation(sourceLocation).build());")
                .closeBlock("}")
                .write()
                .openBlock("public $L() {", getClassName())
                    .write("this($T.NONE);", SourceLocation.class)
                .closeBlock("}")
                .write();
    }

    private void addBuilderConstructor(JavaCodeWriter writer) {
        writer.openBlock("private $L(Builder builder) {", getClassName());
        writer.write("super(NAME, builder().getSourceLocation());");

        for (MemberShape member : shape.getAllMembers().values()) {
            if (member.isRequired()) {
                writer.write("$1L = $2T.requiredState($1S, builder.$1L);", member.getMemberName(), SmithyBuilder.class);
            } else {
                writer.write("$1L = builder.$1L;", member.getMemberName());
            }
        }

        writer.closeBlock("}").write();
    }

    private void addMembers(JavaCodeWriter writer) {
        for (MemberShape member : shape.getAllMembers().values()) {
            writer.write("private final $L $L;", "String", member.getMemberName());
        }

        writer.write();
    }

    private void addGetters(JavaCodeWriter writer) {
        for (MemberShape member : shape.getAllMembers().values()) {
            if (member.isRequired()) {
                writer.openBlock("public $L get$L() {", "String", StringUtils.capitalize(member.getMemberName()));
                writer.write("return $L;", member.getMemberName());
                writer.closeBlock("}");
            } else {
                writer.openBlock("public $T<$L> get$L() {",
                                 Optional.class, "String", StringUtils.capitalize(member.getMemberName()));
                writer.write("return $T.ofNullable($L);", Optional.class, member.getMemberName());
                writer.closeBlock("}");
            }
            writer.write();
        }
    }

    private void addToNode(JavaCodeWriter writer) {
        writer.write("@Override");
        writer.openBlock("protected $T createNode() {", Node.class);
            writer.write("// TODO");
            writer.write("return $T.objectNode();", Node.class);
        writer.closeBlock("}");
        writer.write();
    }

    private void addToBuilder(JavaCodeWriter writer) {
        writer.javadoc(() -> {
            writer.write("Creates a builder used to build $L.", getClassName());
            writer.write();
            writer.write("@returns Returns a builder.");
        });
        writer.openBlock("public static Builder builder() {");
            writer.write("return new Builder();");
        writer.closeBlock("}").write();

        writer.write("@Override");
        writer.openBlock("public Builder toBuilder() {");
            writer.write("// TODO");
            writer.write("return builder();");
        writer.closeBlock("}").write();
    }

    private void addBuilder(JavaCodeWriter writer) {
        String className = getClassName();
        writer.javadoc(() -> writer.write("Builder used to build $L.", getClassName()));

        writer
                .openBlock("public static final class Builder extends $T<$L, Builder> {",
                           AbstractTraitBuilder.class, className)
                    .write("@Override")
                    .openBlock("public $L build() {", className)
                        .write("return new $L(this);", className)
                    .closeBlock("}")
                    .write("// TODO")
                .closeBlock("}").write();
    }

    private void addProvider(JavaCodeWriter writer) {
        writer
                .openBlock("public static final class Provider extends $T.Provider {", AbstractTrait.class)
                    .openBlock("public Provider() {")
                        .write("super(NAME);")
                    .closeBlock("}").write()
                    .write("@Override")
                    .openBlock("public CorsTrait createTrait($T target, $T value) {", ShapeId.class, Node.class)
                        .write("// TODO")
                        .write("return builder().build();")
                    .closeBlock("}")
                .closeBlock("}").write();
    }
}
