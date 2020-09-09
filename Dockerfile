FROM clojure:tools-deps

ENV CODE /code/
RUN mkdir $CODE
WORKDIR $CODE

ADD deps.edn $CODE
RUN clojure -e '(println "Dependencies pulled")'

ADD ./ $CODE
