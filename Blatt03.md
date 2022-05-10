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

a) We perform the following operations on the two machines (it is assumed that
at least one row in `sheet3` already satisfies the condition `id > 5`):

| Machine 1 | Machine 2 |
|-----------|-----------|
| `SELECT * FROM sheet3 where id > 5;` | |
| | `INSERT INTO sheet3 (name) VALUES ("name10")` |
| `SELECT * FROM sheet3 WHERE id > 5;` | |

After performing the insert on M2, we observe
an `AccessShareLock` on the table. Re-running
the `SELECT` instruction on M1 reveals
the newly created row even before committing.

b) After performing an insert on M2,
we observe two `AccessShareLock`s on the table. Re-running
the `SELECT` instruction on M1 doesn't reveal
the newly created row yet. However, new connections can already
see the newly created row. The row only becomes visible
to M1 once M2 is committed. PostgreSQL uses snapshot isolation in its *Repeatable Read* isolation level.
M1 operates on the same snapshot of the database in both `SELECT` instructions.

We would expect the behavior to be different with 2PL. To place a lock on the
initial `SELECT`, either a table lock or a predicate lock (despite predicate locks
having limited feasibility) would be necessary. Then the `INSERT INTO` statement 
of M2 would have to wait to acquire a write lock on the table, which can only 
happen after M1 releases its read lock at the end.

c) We perform the following two transactions on the database:

| Transaction 1 | Transaction 2 |
|---------------|---------------|
| `UPDATE sheet3 SET name = 'name5' WHERE id = 5;`|  |
| | `UPDATE sheet3 SET name = 'name6' WHERE id = 6;` |
| | `UPDATE sheet3 SET name = 'name7' WHERE id = 5;` |

With isolation level *Read Committed*, T2
waits until T1 is committed to update the same row.
After both transactions are committed, the update from T2
persists in the database as it happened after the update from T1.

With isolation level *Serializable*, T2 still waited
for T1 to commit. After it committed however, the second connection threw
an error `[40001] ERROR: could not serialize access due to concurrent update`.
We were able to commit the first change of T2 regardless,
but in the conflict row, the changes made by T1 persisted.

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
an exclusive lock on one of the two rows of the table. With
the third instruction, T2 now has to wait
for T1 to commit in order to proceed. With
the final instruction, T1 has to wait for
the T2 to commit before proceeding. Now both
transactions require each other to commit first before being 
able to proceed, so a deadlock occurs.

## 3.3 Scheduling

a)