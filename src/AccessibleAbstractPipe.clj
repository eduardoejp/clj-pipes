(ns AccessibleAbstractPipe
  (:import java.util.Iterator com.tinkerpop.pipes.AbstractPipe)
  (:gen-class
    :extends com.tinkerpop.pipes.AbstractPipe
    :exposes {starts {:get getStarts}}))
