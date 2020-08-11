# zipper-fs

A Clojure library for navigating a file system using zipper commands `up`,
`down`, `left` and `right`. 

## Usage

``` clojure
(ns zipper-fs.demo
  (:require [zipper-fs.protocols :as p]
            [me.raynes.fs :as fs]))            
(-> (zipper-fs.records/make-fs-node fs/*cwd*)
     p/down
     
     p/right
     p/right
     
     p/down
     p/down
     p/right

     ;; can go back up and it wont need to read the directory list again
     p/up
     p/up
     p/up
     
     p/current
     p/value
     )
```

## License

Copyright © 2020 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
