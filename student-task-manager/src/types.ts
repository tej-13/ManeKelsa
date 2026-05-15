export type Priority = 'Low' | 'Medium' | 'High';

export interface Task {
  id: number;
  title: string;
  dueDate: string;
  subject: string;
  priority: Priority;
  completed: boolean;
}

export interface NewTask {
  title: string;
  dueDate: string;
  subject: string;
  priority: Priority;
}
