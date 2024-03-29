;; # Analysing Chicago bike trips

;; Can we distinguish weekends from weekdays
;; in terms of the typical usage of bikes throughout the day?

;; ## Setup

(ns index
  (:require [tablecloth.api :as tc]
            [tech.v3.datatype.datetime :as datetime]
            [scicloj.noj.v1.vis.hanami :as hanami]
            [aerial.hanami.templates :as ht]
            [scicloj.kindly.v4.kind :as kind]))

;; ## Data reading

(defonce raw-trips
  (-> "data/202304_divvy_tripdata.csv.gz"
      (tc/dataset 
       {:key-fn keyword
        :parser-fn {"started_at" [:local-date-time
                                  "yyyy-MM-dd HH:mm:ss"]
                    "ended_at" [:local-date-time
                                "yyyy-MM-dd HH:mm:ss"]}})))

;; ## Data processing

(def processed-trips
  (-> raw-trips
      (tc/add-columns
       {:hour (fn [ds]
                (->> ds
                     :started_at
                     (datetime/long-temporal-field :hours)))
        :day-of-week (fn [ds]
                       (->> ds
                            :started_at
                            (datetime/long-temporal-field :day-of-week)))})))

;; Let us see our processed trips:

processed-trips

;; ## Data analysis

;; Let us visualize the hour ditsribution of trips:

(defn hours-plot [trips]
  (-> trips 
      (tc/group-by [:hour])
      (tc/aggregate {:n tc/row-count})
      (tc/order-by [:hour])
      (hanami/plot ht/bar-chart
                   {:X "hour"
                    :Y "n"})))

(hours-plot processed-trips)

(kind/portal
 (hours-plot processed-trips))

;; Let us compare the days of the week now:

(-> processed-trips
    (tc/group-by [:day-of-week :hour])
    (tc/aggregate {:n tc/row-count})
    (tc/group-by [:day-of-week])
    (hanami/plot ht/bar-chart
                 {:X "hour"
                  :Y "n"})
    (tc/order-by [:day-of-week]))

;; ## Conclusion

;; Yes, we see that weekends are different from weekdays
;; in terms of the bike trip hour distribution.





