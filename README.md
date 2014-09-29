# Mini YAML Parser #

A simplified YAML-like parser that supports a minimal subset of the YAML spec, using a minimal Java and Go implementation.

*Code license:* Apache License 2.0

***

This parser only reads the following minimal subset of the [YAML spec](http://yaml.org):

    ---  <-- start of document (mandatory)
    #    <-- a comment line, ignored.
         <-- empty lines are ignored (except in multi-line string literals)
    key:          <-- starts a key block in a mapping block (e.g. parent container.)
    key: literal  <-- literal is not typed and internally kept as a string.
    key: |        <-- '|' means everything that follows is a multi-type string till
                      another key of the same indentation OR LESSER is found.
    - [entry]     <-- define an element in a sequence (e.g. an array).
    ...  <-- end of document (mandatory)

This implementation does not support anything not mentioned explicitly above, e.g.
sequences of sequences, or mappings of mappings, (e.g. { } or [ ]), compact nested mapping, tags or references, folded scalars, etc.

Some obvious caveats:

* This is NOT a YAML-compliant parser. It's a subset at best and claims only anecdotal compatibility with the YAML spec.
* Document directives are ignored. In fact anything before or after the document markers (--- and ...) are ignored.
* Only explicit documents are supported so --- and ... are mandatory.
* It's an error to try to mix a sequence (array) and a key mapping in the same block.
* A key can be anything except whitespace and the colon character (:).
* No reflection or Java bean support. Readers uses the underlying list/maps to retrieve values.

You can view this
[unit test data file](https://bitbucket.org/ralfoide/mini-yaml-parser/src/6ec34c7a7b9ca9df1254bcdb95f406ce594b6951/Java/MiniYamlParser/src/tests/com/alfray/utils/test10.yaml?at=master)
to see what's supported and the
[unit test file here](https://bitbucket.org/ralfoide/mini-yaml-parser/src/6ec34c7a7b9ca9df1254bcdb95f406ce594b6951/Java/MiniYamlParser/src/tests/com/alfray/utils/MiniYamlParserTest.java?at=master)
to see an example of usage.

If you need a solid YAML-compliant parser, you can easily find one [elsewhere](http://stackoverflow.com/questions/450399/which-java-yaml-library-should-i-use).

***

The implementation is minimal and consists of only 3 class files. It's designed to be easy to embed as a lightweight utility reader in another project. If you want to contribute enhancements, keep this minimal approach in mind.