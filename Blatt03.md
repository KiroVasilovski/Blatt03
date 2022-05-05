## 3.1 Isolation Levels

a) By default, the isolation mode is *Read Committed*.
PostgreSQL supports the isolation levels *Read Committed*,
*Repeatable Read* and *Serializable*. *Read Uncommitted* can
be specified as well, but internally behaves the same as
*Read Committed*.

c) We perform the following SQL query on the database:

```sql
SELECT * FROM sheet3 WHERE id = 3;

SELECT relation::regclass, mode, granted FROM pg_locks WHERE relation::regclass = 'sheet3'::regclass
```

We observe a lock of type `AccessShareLock`
on the table `sheet3`.

An `AccessShareLock` represents a lock on data that is only read
but not otherwise modified.

d) We again observe a lock of type `AccessShareLock`
on `sheet3`, and additionally an `SIReadLock`, also on `sheet3`.

After committing the transaction and rerunning the second
`SELECT` statement, we observe that all locks have been lifted.

## 3.2 Lock Conflicts

a) After performing an insert on the second machine, we observe
an `AccessShareLock` on the table. Re-running
the `SELECT` instruction on the first machine reveals
the newly created row even before committing.

b) After performing an insert on the second connection,
we observe two `AccessShareLock`s on the table. Re-running
the `SELECT` instruction on the first connection doesn't reveal
the newly created row yet. However, new connections can already
see the newly created row. The row only becomes visible
to the first connection after committing.

c) With isolation level *Read Committed*, the second transaction
waits until the first transaction is committed to update the same row.
After both transactions are committed, the update from the second transaction
persists in the database as it happened after the update from the first
transaction.

With isolation level *Serializable*, the second transaction still waited
for the first to commit. After it committed however, the second connection threw
an error `[40001] ERROR: could not serialize access due to concurrent update`.
We were able to commit the first change of the second commit regardless,
but in the conflict row, the changes made by the first transaction persisted.

d)

e) Yes, we were able to create a deadlock with the following
transactions in *Serializable* isolation:

| Transaction 1 | Transaction 2 |
|---------------|---------------|
| `UPDATE sheet3 SET name = 'name11' WHERE id = 13;`|  |
| | `UPDATE sheet3 SET name = 'name13' WHERE id = 14;` |
| | `UPDATE sheet3 SET name = 'name14' WHERE id = 13;` |
| `UPDATE sheet3 SET name = 'name12' WHERE id = 14;` | |

In the first two instructions, both transactions acquire
an exclusive lock on one of the two rows of the table. In
the third instruction, the second transaction now has to wait
for the first transaction to commit in order to proceed. With
the final instruction, the first transaction has to wait for
the second transaction to commit before proceeding. Now both
transactions require each other to commit first before being 
able to proceed, so a deadlock occurs.

