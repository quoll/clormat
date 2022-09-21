[![Hippocratic License HL3-BDS-CL-ECO-EXTR-FFD-LAW-MEDIA-MIL-MY-SOC-SUP-SV-TAL-USTA-XUAR](https://img.shields.io/static/v1?label=Hippocratic%20License&message=HL3-BDS-CL-ECO-EXTR-FFD-LAW-MEDIA-MIL-MY-SOC-SUP-SV-TAL-USTA-XUAR&labelColor=5e2751&color=bc8c3d)](https://firstdonoharm.dev/version/3/0/bds-cl-eco-extr-ffd-law-media-mil-my-soc-sup-sv-tal-usta-xuar.html)
# clormat

An implementation of the `cljs.core/format` function for ClojureScript. Also runs (redundantly) in Clojure.

The specification is large, so this is an ongoing project.

## Usage

The latest version can be included in `project.clj` as:
[![Clojars Project](http://clojars.org/org.clojars.quoll/clormat/latest-version.svg)](http://clojars.org/org.clojars.quoll/clormat)

Or in `deps.edn` with:
```clojure
{ :deps {
    org.clojars.quoll/clormat {:mvn/version "0.0.1"}
  }
}
```

To use the library, load core and refer to the `format` function. In Clojure this is renamed to `-format` so as not to interfere with `clojure.core/format`:

```clojure
(require '[clormat.core :refer #?{:clj [-format] :cljs [format]])

(-format "%d is %1$#x in hex and %1$#o in octal" 55)

  ;; returns: "55 is 0x37 in hex and 067 in octal"
```

Currently supports:
- Positional parameters: #n$d where _n_ is an argument number indexed from 1.
- Width specifiers.
- %d for integers with flags of: `,( -0`
- %h %H for hash values.
- %s %S for strings.
- %c %C for characters.
- %o for octal values, with negative numbers expressed for all 54 bits on ClojureScript. Flags of `- 0#`
- %x and %X for hexadecimal values, with negative numbers expressed for all 54 bits on ClojureScript. Flags of `- 0#`
- %b %B for booleans with the flag `-`
- %% as the literal `%` character

## TODO
- %e %E scientific notation for floating point
- %f decimal floating point
- %g %G scientific notation OR decimal floating point
- %a %A hexadecimal floating point
- %t %T date/time
- %n platform specific line separator

### For Developers

To run the project's tests Clojure:

```bash
    $ clojure -T:build test
```
Or
```bash
    $ clojure -M:test
```

To run the tests on ClojureScript (configured to use `node.js` by default):

```bash
    $ clojure -M:test-cljs
```

## FAQ

### Why?
I get frustrated at seeing functions in Clojure that aren't available in ClojureScript.

### Why do octal and hex values only go to 54 bits?
This is the size of integers in JavaScript. This is because they are represented using the Mantissa portion of an IEEE-754 64-bit floating point value.

### I could use this for floating point numbers. Why aren't they done yet?
I figured they'd be a bit harder, and I wanted to get an initial release done.

### This license is incompatible with my other code. Can I change it?
So long as you're not using it for anything specified in the license, I'm happy to let you use it under the EPL. Talk to me.

### Why is the name so weird?
It is a portmanteau for "Clojure(Script) Format". It sounded weird, but the more I said it the more amused I was at the name. It makes me think of a Floor Mat in a car. Yes, I know I'm a bit strange.

## License

Copyright © 2022 Paula Gearon

Distributed under the [Hippocratic License](https://firstdonoharm.dev/)
