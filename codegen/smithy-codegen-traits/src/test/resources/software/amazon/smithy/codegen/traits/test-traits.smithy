namespace smithy.example.codegen

/// Indicates that a shape is a string trait.
///
/// Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor. Lorem ipsum dolor.
trait myStringTrait {
    shape: String,
}

trait someAnnotation {
    target: "*",
}

trait customHttp {
    shape: HttpTrait,
}

@private
structure HttpTrait {
    @required
    uri: String,

    @required
    method: String,

    code: Integer,
}
