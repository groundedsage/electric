(ns user.seven-gui-2-temperature-converter
  (:require [hyperfiddle.photon :as p]
            [hyperfiddle.photon-dom :as dom]
            [hyperfiddle.photon-ui :as ui])
  #?(:cljs (:require-macros user.seven-gui-2-temperature-converter)))

;; https://eugenkiss.github.io/7guis/tasks#temp

(defn celsius->farenheit [c] (+ (* c (/ 9 5)) 32))
(defn farenheit->celsius [f] (* (- f 32) (/ 5 9)))

(p/defn App []
  (let [!state      (atom 0)
        temperature (p/watch !state)]
    (dom/div
     (dom/h1 "Temperature Converter")
     (dom/dl
      (dom/dt "Celcius")
      (dom/dd (ui/input {::ui/type        :number
                         ::ui/value       temperature
                         ::dom/step       0.5
                         ::ui/format      "%.2f"
                         ::ui/input-event (p/fn [event]
                                            (let [f (-> event :target :value js/parseFloat)]
                                              (reset! !state f)))}))
      (dom/dt "Farenheit")
      (dom/dd (ui/input {::ui/type        :number
                         ::ui/value       (celsius->farenheit temperature)
                         ::dom/step       0.5
                         ::ui/input-event (p/fn [event]
                                            (let [f (-> event :target :value js/parseFloat)]
                                              (reset! !state (farenheit->celsius f))))}))))))
