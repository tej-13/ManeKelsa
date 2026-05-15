import express from "express";
import { createServer as createViteServer } from "vite";
import Database from "better-sqlite3";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const db = new Database("tasks.db");

// Initialize Database (The "Room" equivalent)
db.exec(`
  CREATE TABLE IF NOT EXISTS tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    dueDate TEXT NOT NULL,
    subject TEXT NOT NULL,
    priority TEXT NOT NULL,
    completed INTEGER DEFAULT 0
  )
`);

async function startServer() {
  const app = express();
  app.use(express.json());

  // API Routes (The "Dao" equivalent)
  app.get("/api/tasks", (req, res) => {
    const tasks = db.prepare("SELECT * FROM tasks ORDER BY dueDate ASC").all();
    res.json(tasks.map(t => ({ ...t, completed: !!t.completed })));
  });

  app.post("/api/tasks", (req, res) => {
    const { title, dueDate, subject, priority } = req.body;
    const info = db.prepare(
      "INSERT INTO tasks (title, dueDate, subject, priority) VALUES (?, ?, ?, ?)"
    ).run(title, dueDate, subject, priority);
    res.json({ id: info.lastInsertRowid, ...req.body, completed: false });
  });

  app.patch("/api/tasks/:id", (req, res) => {
    const { completed } = req.body;
    db.prepare("UPDATE tasks SET completed = ? WHERE id = ?").run(completed ? 1 : 0, req.params.id);
    res.json({ success: true });
  });

  app.delete("/api/tasks/:id", (req, res) => {
    db.prepare("DELETE FROM tasks WHERE id = ?").run(req.params.id);
    res.json({ success: true });
  });

  // Vite integration
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    app.use(express.static(path.join(__dirname, "dist")));
    app.get("*", (req, res) => {
      res.sendFile(path.join(__dirname, "dist", "index.html"));
    });
  }

  const PORT = 3000;
  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server running at http://localhost:${PORT}`);
  });
}

startServer();
