(ns graphql-inquiry.main-test
  (:require [clojure.test :refer [is deftest testing]]
            [graphql-inquiry.main :refer [query mutation]]))

(deftest query-test
  (is (= "{say {hello world}}"
         (query [:say [:hello :world]])))
  (is (= "{say {hello {world}}}"
         (query [:say [:hello [:world]]])))
  (testing "with arguments"
    (is (= "{say (string:\"hae maga\",number:5,true:true,false:false,null:null,map:{some:\"key\",submap:{a:1}},vector:[\"hi\",1],list:[\"listy\",false]) {hello world}}"
           (query [:say {:string "hae maga"
                         :number 5
                         :true true
                         :false false
                         :null nil
                         :map {:some "key", :submap {:a 1}}
                         :vector ["hi", 1]
                         :list (list "listy" false)}
                   [:hello :world]]))))
  (testing "with variable-defs"
    (is (= "query ($id:ID!,$greeting:String!) {say (string:\"hae maga\",id:$id) {hello (greeting:$greeting) world}}"
           (query {:query [:say {:string "hae maga"
                                 :id :id} [:hello {:greeting :greeting} :world]]
                   :variable-defs {:id :ID!
                                   :greeting :String!}}))))
  (testing "with aliases"
    (is (= "{howdy:say {hello world}}"
           (query [:howdy:say [:hello :world]]))))
  (testing "with lists and sequences"
    (is (= "{say {hello {world}}}"
           (query [:say (list :hello (filter identity [:world]))]))))
  (testing "metafields"
    (is (= "{say {hello world __typename}}"
           (query [:say [:hello :world :__typename]]))))
  (testing "operation-name"
    (is (= "query OperationVittles {say {hello world}}"
           (query {:query [:say [:hello :world]]
                   :operation-name "OperationVittles"})))
    (is (= "query OperationNeptuneSpear($id:ID!) {say {hello world}}"
           (query {:query [:say [:hello :world]]
                   :variable-defs {:id :ID!}
                   :operation-name "OperationNeptuneSpear"})))))

(deftest mutation-test
  (is (= "mutation ($id:ID!,$customer:Customer!) {updateCustomer (id:$id,customer:$customer) {name email}}"
         (mutation {:query [:updateCustomer {:id :id
                                             :customer :customer} [:name :email]]
                    :variable-defs {:id :ID!
                                    :customer :Customer!}})))
  (testing "operation-name"
    (is (= "mutation OperationValkyrie($id:ID!,$customer:Customer!) {updateCustomer (id:$id,customer:$customer) {name email}}"
           (mutation {:query [:updateCustomer {:id :id
                                               :customer :customer} [:name :email]]
                      :variable-defs {:id :ID!
                                      :customer :Customer!}
                      :operation-name "OperationValkyrie"})))))
