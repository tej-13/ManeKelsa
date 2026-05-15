import React, { useState, useEffect } from 'react';
import { Plus, CheckCircle2, Circle, Trash2, Calendar, BookOpen, X } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { Task, NewTask, Priority } from './types';

export default function App() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [isAdding, setIsAdding] = useState(false);
  const [loading, setLoading] = useState(true);
  const [newTask, setNewTask] = useState<NewTask>({
    title: '',
    dueDate: new Date().toISOString().split('T')[0],
    subject: '',
    priority: 'Medium'
  });

  useEffect(() => {
    fetchTasks();
  }, []);

  const fetchTasks = async () => {
    try {
      const res = await fetch('/api/tasks');
      const data = await res.json();
      setTasks(data);
    } catch (err) {
      console.error('Failed to fetch tasks', err);
    } finally {
      setLoading(false);
    }
  };

  const addTask = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTask.title || !newTask.subject) return;

    try {
      const res = await fetch('/api/tasks', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newTask)
      });
      const data = await res.json();
      setTasks([...tasks, data]);
      setIsAdding(false);
      setNewTask({
        title: '',
        dueDate: new Date().toISOString().split('T')[0],
        subject: '',
        priority: 'Medium'
      });
    } catch (err) {
      console.error('Failed to add task', err);
    }
  };

  const toggleTask = async (id: number, completed: boolean) => {
    try {
      await fetch(`/api/tasks/${id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ completed: !completed })
      });
      setTasks(tasks.map(t => t.id === id ? { ...t, completed: !completed } : t));
    } catch (err) {
      console.error('Failed to update task', err);
    }
  };

  const deleteTask = async (id: number) => {
    try {
      await fetch(`/api/tasks/${id}`, { method: 'DELETE' });
      setTasks(tasks.filter(t => t.id !== id));
    } catch (err) {
      console.error('Failed to delete task', err);
    }
  };

  const getPriorityColor = (priority: Priority) => {
    switch (priority) {
      case 'High': return 'text-rose-500 bg-rose-50 border-rose-100';
      case 'Medium': return 'text-amber-500 bg-amber-50 border-amber-100';
      case 'Low': return 'text-emerald-500 bg-emerald-50 border-emerald-100';
    }
  };

  return (
    <div className="min-h-screen bg-[#FDFCF9] text-[#1C1B17] font-sans selection:bg-[#EADDFF]">
      {/* Header */}
      <header className="sticky top-0 z-10 bg-[#FDFCF9]/80 backdrop-blur-md px-6 py-8 flex justify-between items-end max-w-2xl mx-auto w-full">
        <div>
          <h1 className="text-4xl font-semibold tracking-tight">Tasks</h1>
          <p className="text-[#49454F] mt-1 font-medium">
            {tasks.filter(t => !t.completed).length} pending for today
          </p>
        </div>
        <motion.button
          whileTap={{ scale: 0.95 }}
          onClick={() => setIsAdding(true)}
          className="bg-[#D0BCFF] text-[#381E72] p-4 rounded-2xl shadow-sm hover:shadow-md transition-shadow"
        >
          <Plus size={24} />
        </motion.button>
      </header>

      <main className="max-w-2xl mx-auto px-6 pb-24">
        {loading ? (
          <div className="flex justify-center py-20">
            <div className="w-8 h-8 border-4 border-[#D0BCFF] border-t-transparent rounded-full animate-spin" />
          </div>
        ) : (
          <div className="space-y-4">
            <AnimatePresence mode="popLayout">
              {tasks.map((task) => (
                <motion.div
                  key={task.id}
                  layout
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, scale: 0.95 }}
                  className={`group relative bg-white border border-[#CAC4D0] rounded-2xl p-5 flex items-start gap-4 transition-all hover:border-[#6750A4] ${task.completed ? 'opacity-60' : ''}`}
                >
                  <button
                    onClick={() => toggleTask(task.id, task.completed)}
                    className="mt-1 text-[#6750A4] hover:scale-110 transition-transform"
                  >
                    {task.completed ? <CheckCircle2 size={24} /> : <Circle size={24} />}
                  </button>

                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <span className={`text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full border ${getPriorityColor(task.priority)}`}>
                        {task.priority}
                      </span>
                      <span className="text-xs text-[#49454F] flex items-center gap-1">
                        <BookOpen size={12} /> {task.subject}
                      </span>
                    </div>
                    <h3 className={`text-lg font-medium leading-tight truncate ${task.completed ? 'line-through text-[#49454F]' : ''}`}>
                      {task.title}
                    </h3>
                    <div className="flex items-center gap-3 mt-2 text-[#49454F] text-sm">
                      <span className="flex items-center gap-1">
                        <Calendar size={14} /> {new Date(task.dueDate).toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}
                      </span>
                    </div>
                  </div>

                  <button
                    onClick={() => deleteTask(task.id)}
                    className="opacity-0 group-hover:opacity-100 p-2 text-[#BA1A1A] hover:bg-rose-50 rounded-full transition-all"
                  >
                    <Trash2 size={20} />
                  </button>
                </motion.div>
              ))}
            </AnimatePresence>

            {tasks.length === 0 && !isAdding && (
              <div className="text-center py-20">
                <div className="bg-[#F3EDF7] w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                  <CheckCircle2 size={32} className="text-[#6750A4]" />
                </div>
                <h3 className="text-xl font-medium">All caught up!</h3>
                <p className="text-[#49454F]">Enjoy your free time, student.</p>
              </div>
            )}
          </div>
        )}
      </main>

      {/* Add Task Modal */}
      <AnimatePresence>
        {isAdding && (
          <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-6">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setIsAdding(false)}
              className="absolute inset-0 bg-black/40 backdrop-blur-sm"
            />
            <motion.div
              initial={{ y: '100%' }}
              animate={{ y: 0 }}
              exit={{ y: '100%' }}
              transition={{ type: 'spring', damping: 25, stiffness: 200 }}
              className="relative bg-[#FDFCF9] w-full max-w-lg rounded-t-[28px] sm:rounded-[28px] p-8 shadow-2xl"
            >
              <div className="flex justify-between items-center mb-8">
                <h2 className="text-2xl font-semibold">New Task</h2>
                <button onClick={() => setIsAdding(false)} className="p-2 hover:bg-[#F3EDF7] rounded-full">
                  <X size={24} />
                </button>
              </div>

              <form onSubmit={addTask} className="space-y-6">
                <div className="space-y-2">
                  <label className="text-sm font-medium text-[#49454F] ml-1">Title</label>
                  <input
                    autoFocus
                    type="text"
                    placeholder="e.g., Finish Calculus Assignment"
                    className="w-full bg-[#F3EDF7] border-none rounded-2xl px-5 py-4 text-lg focus:ring-2 focus:ring-[#6750A4] outline-none transition-all"
                    value={newTask.title}
                    onChange={e => setNewTask({ ...newTask, title: e.target.value })}
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-[#49454F] ml-1">Subject</label>
                    <input
                      type="text"
                      placeholder="Math, Physics..."
                      className="w-full bg-[#F3EDF7] border-none rounded-2xl px-5 py-4 focus:ring-2 focus:ring-[#6750A4] outline-none transition-all"
                      value={newTask.subject}
                      onChange={e => setNewTask({ ...newTask, subject: e.target.value })}
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-[#49454F] ml-1">Due Date</label>
                    <input
                      type="date"
                      className="w-full bg-[#F3EDF7] border-none rounded-2xl px-5 py-4 focus:ring-2 focus:ring-[#6750A4] outline-none transition-all"
                      value={newTask.dueDate}
                      onChange={e => setNewTask({ ...newTask, dueDate: e.target.value })}
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <label className="text-sm font-medium text-[#49454F] ml-1">Priority</label>
                  <div className="flex gap-2">
                    {(['Low', 'Medium', 'High'] as Priority[]).map(p => (
                      <button
                        key={p}
                        type="button"
                        onClick={() => setNewTask({ ...newTask, priority: p })}
                        className={`flex-1 py-3 rounded-xl border-2 transition-all font-medium ${newTask.priority === p ? 'bg-[#EADDFF] border-[#6750A4] text-[#21005D]' : 'bg-transparent border-[#CAC4D0] text-[#49454F]'}`}
                      >
                        {p}
                      </button>
                    ))}
                  </div>
                </div>

                <button
                  type="submit"
                  className="w-full bg-[#6750A4] text-white py-4 rounded-full text-lg font-semibold shadow-lg hover:shadow-xl hover:bg-[#4F378B] transition-all mt-4"
                >
                  Create Task
                </button>
              </form>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}
