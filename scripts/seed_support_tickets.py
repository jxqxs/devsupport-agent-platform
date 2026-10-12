"""向 support_tickets 插入 10 万条虚拟工单，只用于索引实验。"""

import random
from datetime import datetime, timedelta

import pymysql

TOTAL = 100_000
BATCH_SIZE = 1000
CATEGORIES = ("BUG", "FEATURE", "TASK")
STATUSES = ("OPEN", "CLOSED", "PENDING")
PRIORITIES = ("LOW", "MEDIUM", "HIGH")
START = datetime(2025, 1, 1)
SQL = """
INSERT INTO support_tickets (title, category, status, priority, created_at)
VALUES (%s, %s, %s, %s, %s)
"""


def make_row(index):
    created_at = START + timedelta(seconds=random.randint(0, 365 * 24 * 3600))
    return (
        f"工单{index}",
        random.choice(CATEGORIES),
        random.choice(STATUSES),
        random.choice(PRIORITIES),
        created_at,
    )


def main():
    connection = pymysql.connect(
        host="127.0.0.1",
        port=3306,
        user="root",
        password="123456",
        database="devsupport_lab",
    )
    try:
        with connection.cursor() as cursor:
            batch = []
            for index in range(TOTAL):
                batch.append(make_row(index))
                if len(batch) == BATCH_SIZE:
                    cursor.executemany(SQL, batch)
                    connection.commit()
                    batch.clear()
            if batch:
                cursor.executemany(SQL, batch)
                connection.commit()
        with connection.cursor() as cursor:
            cursor.execute("SELECT COUNT(*) FROM support_tickets")
            count = cursor.fetchone()[0]
        print(f"support_tickets={count}")
    finally:
        connection.close()


if __name__ == "__main__":
    main()
