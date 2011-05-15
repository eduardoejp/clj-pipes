;Version: 0.1.0
;Copyright: Eduardo Emilio Julián Pereyra, 2011
;Email: eduardoejp@gmail.com
;License: EPL 1.0 -> http://www.eclipse.org/legal/epl-v10.html

(ns clj-pipes
  #^{:author "Eduardo Emilio Julián Pereyra",
     :doc "Wrapper for the TinkerPop Pipes API for Graph DBMSs. It supports version 0.5 of the Pipes API."}
  (:import (com.tinkerpop.pipes AbstractPipe Pipeline IdentityPipe
             MultiIterator EmptyIterator ExpandableIterator HistoryIterator SingleIterator)
    (com.tinkerpop.pipes.pgm LabelFilterPipe LabelPipe
      IdPipe IdFilterPipe
      GraphElementPipe GraphElementPipe$ElementType
      PropertyPipe PropertyFilterPipe PropertyMapPipe
      BothEdgesPipe BothPipe BothVerticesPipe
      InEdgesPipe InPipe InVertexPipe
      OutEdgesPipe OutPipe OutVertexPipe)
    (com.tinkerpop.pipes.filter FilterPipe ComparisonFilterPipe$Filter
      ObjectFilterPipe RangeFilterPipe UniquePathFilterPipe RandomFilterPipe DuplicateFilterPipe CollectionFilterPipe
      AndFilterPipe OrFilterPipe BackFilterPipe FutureFilterPipe)
    (com.tinkerpop.pipes.branch CopySplitPipe ExhaustiveMergePipe FairMergePipe)
    (com.tinkerpop.pipes.sideeffect SideEffectPipe CountPipe AggregatorPipe GroupCountPipe SideEffectCapPipe)
    (com.tinkerpop.pipes.util HasNextPipe ScatterPipe PathPipe GatherPipe HasCountPipe)
    (java.util NoSuchElementException)
    AccessibleAbstractPipe)
  (:refer-clojure :exclude [identity count]))

; Utility fns
(defn get-side-effect
  "Gets the side effect from a SideEffectPipe."
  [#^SideEffectPipe p] (.getSideEffect p))

(defn get-pipes
  "Returns the pipes a MetaPipe is holding."
  [meta-pipe] (-> meta-pipe .getPipes seq))

(defn reset-pipe "Resets a Pipe that may be storing some inner data."
  [pipe] (.reset pipe))

(defn pipeline
  "Buils a pipeline from the given pipes."
  [& pipes] (Pipeline. pipes))

(defn make-pipe
  "Creates a new pipe that applies the given fn to their arguments."
  [f]
  (proxy [AccessibleAbstractPipe] []
    (processNextStart [] (f (-> this .getStarts .next)))))

(defn make-filter
  "Returns a FilterPipe that lets its arguments pass if (f item) is neither nil nor false."
  [f]
  (proxy [AccessibleAbstractPipe] []
    (processNextStart []
      (loop [item (-> this .getStarts .next)]
        (if (f item) item (recur (-> this .getStarts .next)))))))

(defn set-start
  "Given a sequence, a vector or an individual item, sets it as the start of the given pipe."
  [pipe coll]
  (if (or (seq? coll) (vector? coll))
    (.setStarts pipe coll)
    (.setStarts pipe [coll]))
  pipe)

; Iterators
(defn empty-iter "" [] (EmptyIterator.))

(defn expandable-iter "" [iter] (ExpandableIterator. iter))

(defn multi-iter "" [& iters] (MultiIterator. iters))

(defn history-iter "" [iter] (HistoryIterator. iter))

(defn single-iter "" [object] (SingleIterator. object))

; Util Pipes
(defn path "" [] (PathPipe.))

(defn scatter "" [] (ScatterPipe.))

(defn gather "" [] (GatherPipe.))

(defn has-count "" [min max] (HasCountPipe. min max))

(defn has-next "" [pipe] (HasNextPipe. pipe))

; Regular Pipes
(defn graph-elem "" [elem] (GraphElementPipe. (case elem :vertex GraphElementPipe$ElementType/VERTEX, :edge GraphElementPipe$ElementType/EDGE)))

(defn property "" [kprop] (PropertyPipe. (name kprop)))

(defn property-map "" [] (PropertyMapPipe.))

(defn identity "" [] (IdentityPipe.))

(defn label "" [] (LabelPipe.))

(defn side-effect-cap "" [se-pipe] (SideEffectCapPipe. se-pipe))

(defn vertices
  "Given the direction as either :in, :out or :both, returns as suitable edges->vertices pipe."
  [direction]
  (case direction
    :in (InVertexPipe.)
    :out (OutVertexPipe.)
    :both (BothVerticesPipe.)))

(defn edges
  "Given the direction as either :in, :out or :both (and an optional label as a keyword), returns as suitable vertices->edges pipe."
  ([direction klabel]
   (if klabel
     (case direction
       :in (InEdgesPipe. (name klabel))
       :out (OutEdgesPipe. (name klabel))
       :both (BothEdgesPipe. (name klabel)))
     (case direction
       :in (InEdgesPipe.)
       :out (OutEdgesPipe.)
       :both (BothEdgesPipe.))))
  ([direction] (edges direction nil)))

(defn ends
  "Given the direction as either :in, :out or :both (and an optional label as a keyword), returns as suitable vertices->vertices pipe."
  ([direction klabel]
   (if klabel
     (case direction
       :in (InPipe. (name klabel))
       :out (OutPipe. (name klabel))
       :both (BothPipe. (name klabel)))
     (case direction
       :in (InPipe.)
       :out (OutPipe.)
       :both (BothPipe.))))
  ([direction] (ends direction nil)))

(defn id "" [] (IdPipe.))

; Filtering Pipes
(defn- get-comparison-filter [cf]
  "Inverted the way comparison fns and filters are matched, so they work as in Clojure, not as in Pipes."
  (cond
    (= cf not=) ComparisonFilterPipe$Filter/EQUAL, (= cf =) ComparisonFilterPipe$Filter/NOT_EQUAL,
    (= cf >=) ComparisonFilterPipe$Filter/LESS_THAN, (= cf <=) ComparisonFilterPipe$Filter/GREATER_THAN,
    (= cf >) ComparisonFilterPipe$Filter/LESS_THAN_EQUAL, (= cf <) ComparisonFilterPipe$Filter/GREATER_THAN_EQUAL))

(defn property-filter
  "Given a comparison operation (one of the Clojure comparison fns #{=, not=, <, >, <=, >=}), filters according to a given property."
  [op k v] (PropertyFilterPipe. (name k) v (get-comparison-filter op)))

(defn label-filter "" [klabel filter] (LabelFilterPipe. (name klabel) (get-comparison-filter filter)))

(defn range-filter "" [low high] (RangeFilterPipe. low high))

(defn collection-filter
  "Given a comparison operation (one of the Clojure comparison fns #{=, not=, <, >, <=, >=}), creates a suitable CollectionFilterPipe."
  [coll op] (CollectionFilterPipe. coll (get-comparison-filter op)))

(defn duplicate-filter "" [] (DuplicateFilterPipe.))

(defn object-filter
  "Given a comparison operation (one of the Clojure comparison fns #{=, not=, <, >, <=, >=}), filters according to the relation to the given object."
  [obj op] (ObjectFilterPipe. obj (get-comparison-filter op)))

(defn random-filter "" [bias] (RandomFilterPipe. bias))

(defn unique-path-filter "" [] (UniquePathFilterPipe.))

(defn back-filter "" [filter] (BackFilterPipe. filter))

(defn future-filter "" [filter] (FutureFilterPipe. filter))

(defn id-filter "" [id comp] (IdFilterPipe. id (get-comparison-filter comp)))

; Meta Pipes
(defn and-filter "" [& pipes] (AndFilterPipe. pipes))

(defn or-filter "" [& pipes] (OrFilterPipe. pipes))

; Side-Effect Pipes
(defn aggregator "" [#^java.util.Collection coll] (AggregatorPipe. coll))

(defn count "" [] (CountPipe.))

(defn group-count "" ([] (GroupCountPipe.)) ([count-map] (GroupCountPipe. count-map)))

; Branching pipes
(defn copy-split "" [& pipes] (CopySplitPipe. pipes))

(defn exhaustive-merge "" [& pipes] (ExhaustiveMergePipe. pipes))

(defn fair-merge "" [& pipes] (FairMergePipe. pipes))
