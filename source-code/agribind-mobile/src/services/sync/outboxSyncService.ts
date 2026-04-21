import db from "../../db/database";

const API_BASE_URL = "http://localhost:8081/api/communications";

export const enqueueOfflineAction = (
  clientOutboxId: string,
  actionType: string,
  payload: Record<string, unknown>
) => {
  db.runSync(
    `INSERT OR REPLACE INTO offline_transaction_queue
      (client_outbox_id, action_type, payload, status, created_at, attempts)
     VALUES (?, ?, ?, 'PENDING', ?, 0)`,
    [clientOutboxId, actionType, JSON.stringify(payload), new Date().toISOString()]
  );
};

export const synchronizeOfflineData = async () => {
  const rows = db.getAllSync<{
    client_outbox_id: string;
    action_type: string;
    payload: string;
    created_at: string;
  }>(
    "SELECT client_outbox_id, action_type, payload, created_at FROM offline_transaction_queue WHERE status='PENDING' ORDER BY created_at ASC"
  );

  for (const row of rows) {
    const payload = JSON.parse(row.payload);
    const response = await fetch(`${API_BASE_URL}/sync`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        clientOutboxId: row.client_outbox_id,
        actionType: row.action_type,
        payload,
        deviceId: "expo-mobile",
        timestamp: row.created_at,
      }),
    });

    if (response.status === 200) {
      db.runSync("DELETE FROM offline_transaction_queue WHERE client_outbox_id = ?", [
        row.client_outbox_id,
      ]);
      continue;
    }

    if (response.status === 409) {
      const conflict = await response.json();
      const conflictResolutionResponse = await fetch(
        `${API_BASE_URL}/sync/conflict-resolution`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            clientOutboxId: row.client_outbox_id,
            actionType: row.action_type,
            localVersion: payload,
            serverVersion: conflict.serverVersion ?? payload,
            resolutionMethod: "SERVER_WINS",
          }),
        }
      );
      if (conflictResolutionResponse.ok) {
        db.runSync("DELETE FROM offline_transaction_queue WHERE client_outbox_id = ?", [
          row.client_outbox_id,
        ]);
      }
    }
  }
};
