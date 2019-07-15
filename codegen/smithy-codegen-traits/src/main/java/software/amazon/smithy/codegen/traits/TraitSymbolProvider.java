package software.amazon.smithy.codegen.traits;

import software.amazon.smithy.codegen.core.Symbol;
import software.amazon.smithy.codegen.core.SymbolProvider;
import software.amazon.smithy.model.shapes.Shape;

/**
 * Symbol provider used by the shapes generated for traits.
 */
public class TraitSymbolProvider implements SymbolProvider {
    @Override
    public Symbol toSymbol(Shape shape) {
        throw new UnsupportedOperationException();
    }
}
