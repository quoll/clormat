[![Hippocratic License HL3-BDS-CL-ECO-EXTR-FFD-LAW-MEDIA-MIL-MY-SOC-SUP-SV-TAL-USTA-XUAR](https://img.shields.io/static/v1?label=Hippocratic%20License&message=HL3-BDS-CL-ECO-EXTR-FFD-LAW-MEDIA-MIL-MY-SOC-SUP-SV-TAL-USTA-XUAR&labelColor=5e2751&color=bc8c3d)](https://firstdonoharm.dev/version/3/0/bds-cl-eco-extr-ffd-law-media-mil-my-soc-sup-sv-tal-usta-xuar.html)
# clormat

An implementation of the `cljs.core/format` function for ClojureScript.

The specification is large, so this is an ongoing project.

## Usage

### TODO
Everything from here below is yet to be done.

---

Invoke a library API function from the command-line:

    $ clojure -X clormat.core/-format '"Hello %s %d times"' '"world"' 3
    "Hello world 3 times"

Run the project's tests (they'll fail until you edit them):

    $ clojure -T:build test

Run the project's CI pipeline and build a JAR (this will fail until you edit the tests to pass):

    $ clojure -T:build ci

This will produce an updated `pom.xml` file with synchronized dependencies inside the `META-INF`
directory inside `target/classes` and the JAR in `target`. You can update the version (and SCM tag)
information in generated `pom.xml` by updating `build.clj`.

Install it locally (requires the `ci` task be run first):

    $ clojure -T:build install

Deploy it to Clojars -- needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment
variables (requires the `ci` task be run first):

    $ clojure -T:build deploy

Your library will be deployed to org.clojars.quoll/clormat on clojars.org by default.

## License

Copyright © 2022 Paula Gearon

Distributed under the Hippocratic License
