# Prez: Reveal.js Presentation Generator #

Write Reveal.js presentations using Clojure. Supports a local web server, pre-parsing Markdown, and compiling presentations into standalone, offline-ready HTML files.

## Running

To create a single, standalone presentation file at `target/classes/public/index.html`, run:

```
lein run compile
```

During development, you can just run `prez.core/-main` without any arguments and start a local server like this:

```
lein ring server
```

## License

Copyright © 2013 Daniel L. Gregoire
