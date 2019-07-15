package software.amazon.smithy.codegen.traits;

import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.Model;

public class StructureTraitGeneratorTest {
    @Test
    public void generatesAnnotationTraits() {
        Model model = Model.assembler()
                .addImport(getClass().getResource("test-traits.smithy"))
                .assemble()
                .unwrap();

        TraitGenerator generator = TraitGenerator.create(model, "smithy.example.codegen#customHttp");

        System.out.println(generator.getCode());
    }
}
