package software.amazon.smithy.jsonschema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.IntegerShape;
import software.amazon.smithy.model.shapes.ListShape;
import software.amazon.smithy.model.shapes.MemberShape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.shapes.StringShape;
import software.amazon.smithy.model.shapes.StructureShape;

public class DeconflictingStrategyTest {
    @Test
    public void canDeconflictNamesWhereListsAreActuallyDifferent() {
        StringShape str = StringShape.builder().id("com.bar#String").build();
        MemberShape memberA = MemberShape.builder().id("com.bar#Page$member").target("com.bar#String").build();
        ListShape a = ListShape.builder().id("com.bar#Page").member(memberA).build();
        IntegerShape integer = IntegerShape.builder().id("com.foo#Int").build();
        MemberShape memberB = MemberShape.builder().id("com.foo#Page$member").target("com.foo#Int").build();
        ListShape b = ListShape.builder().id("com.foo#Page").member(memberB).build();
        Model model = Model.builder().addShapes(str, integer, a, b, memberA, memberB).build();

        PropertyNamingStrategy propertyNamingStrategy = PropertyNamingStrategy.createDefaultStrategy();
        RefStrategy strategy = RefStrategy.createDefaultStrategy(model, Node.objectNode(), propertyNamingStrategy);
        assertThat(strategy.toPointer(a.getId()), equalTo("#/definitions/Page"));
        assertThat(strategy.toPointer(b.getId()), equalTo("#/definitions/PageComFoo"));
    }

    @Test
    public void detectsUnsupportedConflicts() {
        StructureShape a = StructureShape.builder().id("com.bar#Page").build();
        StructureShape b = StructureShape.builder().id("com.foo#Page").build();
        Model model = Model.builder().addShapes(a, b).build();
        PropertyNamingStrategy propertyNamingStrategy = PropertyNamingStrategy.createDefaultStrategy();

        Assertions.assertThrows(ConflictingShapeNameException.class, () -> {
            RefStrategy.createDefaultStrategy(model, Node.objectNode(), propertyNamingStrategy);
        });
    }

    @Test
    public void deconflictingStrategyPassesThroughToDelegate() {
        ObjectNode config = Node.objectNode();
        Model model = Model.builder().build();
        PropertyNamingStrategy propertyNamingStrategy = PropertyNamingStrategy.createDefaultStrategy();
        RefStrategy strategy = RefStrategy.createDefaultStrategy(model, config, propertyNamingStrategy);

        assertThat(strategy.toPointer(ShapeId.from("com.foo#Nope")), equalTo("#/definitions/Nope"));
    }
}
