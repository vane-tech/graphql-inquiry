# GraphQL-Inquiry

A tiny library to generate [GraphQL queries](https://graphql.org/) from Clojure data structures.

Heavily inspired by [Poor man’s GraphQL client for ClojureScript apps](https://medium.com/@kirill.ishanov/poor-mans-graphql-client-for-clojurescript-apps-8dc4b04e8738) by Kirill Ishanov. Thank you very much!

## Philosophy

* Tiny, quick, and consise!
* Just generate the query string, leave the https request to the user.
* Support the 80% of GraphQL that we need in our daily work: queries and mutations, fields including aliases, arguments and variables. If you need more, have a look at [graphql-query](https://github.com/district0x/graphql-query) or [graphql-builder](https://github.com/retro/graphql-builder). Or just write a simple string for that one case?
* Neither try to hide GraphQL nor prevent generating invalid queries. This allows users who understand GraphQL to write really consise queries. The server will validate the query.
* Works with Clojure and ClojureScript

## Usage

```Clojure
(require '[graphql-inquiry.main :as graphql])
```

### Queries

The easiest way to start with graphql-inquiry, is simple's query generation.

```Clojure
(graphql/query [:employee {:id 1
                           :active true} [:name :address :friends [:name :email]]])
=> "{employee (id:1,active:true) {name address friends {name email}}}"
```

Obviously, If we would like to fetch employees and projects within the same simple query, we would do it this way:

```Clojure
(graphql/query [:employee {:id 1
                           :active true} [:name :address :friends [:name :email]]
                :projects {:active true} [:customer :price]])
```

### Field arguments

In the example above, `:employee` and `:projects` fields have arguments `{:id 1 :active true}` and `{:id 1 :active true}` respectively.

We can add arguments to other fields easily by wrapping field name and its arguments to vector `[:customer {:id 2}]`:

```Clojure
(graphql/query [:projects {:active true} [:customer {:id 2} :price]])

=> "{projects (active:true) {customer (id:2) price}}"
```

### Query with alias

If you know how aliases work in GraphQL, you know how to do them in graphql-inquiry:

```Clojure
(graphql/query [:workhorse:employee {:id 1, :active true} [:name :address]
                :boss:employee {:id 2, :active true} [:name :address]])
```

Prettified query:
```graphql
{
  workhorse:employee (id: 1, active: true) {
    name
    address
  }
  boss:employee (id: 2, active: true) {
    name
    address
  }
}
```

This of course works just fine at any level!
```Clojure
(graphql/query [:employee {:id 1, :active true} [:name
                                                 :address
                                                 :mates:friends [:name :email]]])
```
Prettified query:
```graphql
{
  employee (id: 1, active: true) {
    name
    address
    mates:friends {name email}
  }
}
```

### Query with variables

If you want to define variables to the query, you need to pass a map to the `query` function. When using them as parameters, just pass them as a keyword. For example, `:id` will become `$id`.

```Clojure
(graphql/query {:query [:workhorse:employee {:id :id
                                             :active true
                                             :name :name} [:name :address]
                        :boss:employee {:id :id, :active true} [:name :address]]
                :variables {:id :ID!
                            :name :String}})
```
Prettified query:
```graphql
query ($id: ID!, $name: String) {
  workhorse:employee (id: $id, active: true, name: $name) {
    name
    address
  }
  boss:employee (id: $id, active: true) {
    name
    address
  }
}
```

### Mutations

Mutations work just like queries with variables, but instead of `query`, you call `mutation`:

```Clojure
(graphql/mutation {:variables {:id :ID!
                               :project :ProjectInput!}
                   :query [:addProjectToEmployee {:employeeId :id, :project :project} [:allocation :name]]})
```
Prettified query:
```graphql
mutation ($id: ID!, $project: ProjectInput!) {
  addProjectToEmployee (employeeId: $id, project: $project) {
    allocation
    name
  }
}
```

### Meta fields

As you probably expect by now, there is no special syntax for meta fields. Just query them the same as any other field:

```Clojure
(query [:say [:hello :world :__typename]])
```
Prettified query:
```graphql
{
  say {
    hello
    world
    __typename
  }
}
```

## Release a new version

1. First, adjust the `<version>` in the [pom.xml](./pom.xml).
2. Now, run `bin/dclojure -Spom` to update the dependencies.
3. Next, generate the jar with `bin/dclojure -A:pack mach.pack.alpha.skinny --no-libs --project-path graphql-inquiry.jar`
4. Publish the jar on Clojars with `docker-compose run -e CLOJARS_USERNAME=billfront -e CLOJARS_PASSWORD=<token> code clojure -A:deploy`. Get a token from [Clojars dashboard](https://clojars.org/tokens).

## MIT License

Copyright 2020 BillFront GmbH

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
