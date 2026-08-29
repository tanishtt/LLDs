Requirements
* ------------
* 1. User can create an alert for a stock.
* 2. Alert can have conditions:
*      - Price > X
*      - Price < X
*      - Percentage change >= X
*      - Composite AND / OR conditions
* 3. Alert can notify through:
*      - EMAIL
*      - SMS
*      - PUSH
* 4. Multiple users can watch the same stock.
* 5. One user can watch multiple stocks.
* 6. Concurrent price updates should be supported.
* 7. Price updates for the same stock must be processed in order.
* 8. Same condition should not repeatedly notify while it remains true.
* 9. Notification rate limiting per user.
* 10. Notification delivery should not block price processing.
* 11. New conditions/channels should be easy to add.


* Design Patterns
* ---------------
* Observer      -> Stock -> PriceAlert
* Strategy      -> AlertCondition
* Strategy      -> NotificationChannel
* Factory       -> NotificationChannelFactory
* Decorator     -> RetryingNotificationChannel
* Repository    -> StockRegistry / AlertRepository
* Facade        -> StockNotificationService
*
* Concurrency
* -----------
* - ConcurrentHashMap for shared registries
* - Per-stock single-threaded executor for ordered price processing
* - AtomicBoolean for alert armed/disarmed state
* - BlockingQueue based notification executor
*
* NOTE:
* This is an in-memory LLD. Production systems would usually
* replace these components with Kafka, Redis, DB, etc.
*/