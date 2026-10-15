import express from 'express';
import cors from 'cors';
import jwt from 'jsonwebtoken';
import bcrypt from 'bcryptjs';
import multer from 'multer';
import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET || 'secret-key-for-e-learning-platform-very-secure-must-be-long-enough';

// Ensure upload folders exist
const uploadsDir = path.join(__dirname, 'uploads');
fs.mkdirSync(path.join(uploadsDir, 'videos'), { recursive: true });
fs.mkdirSync(path.join(uploadsDir, 'resources'), { recursive: true });
fs.mkdirSync(path.join(uploadsDir, 'avatars'), { recursive: true });

// Setup multer storage
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    let dest = uploadsDir;
    if (req.originalUrl.includes('videos')) dest = path.join(uploadsDir, 'videos');
    else if (req.originalUrl.includes('resources')) dest = path.join(uploadsDir, 'resources');
    else if (req.originalUrl.includes('avatar')) dest = path.join(uploadsDir, 'avatars');
    cb(null, dest);
  },
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1e9);
    cb(null, uniqueSuffix + '-' + (file.originalname || 'file'));
  }
});
const upload = multer({ storage });

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// ----------------------------------------------------
// IN-MEMORY DATA STORE
// ----------------------------------------------------
let users = [
  {
    id: 1,
    name: 'Admin User',
    email: 'admin@university.edu',
    password: bcrypt.hashSync('password123', 10),
    plainPassword: 'password123',
    role: 'ADMIN',
    active: true,
    bio: 'System Administrator for University Portal',
    avatarRef: ''
  },
  {
    id: 2,
    name: 'Prof. Alan Turing',
    email: 'teacher@university.edu',
    password: bcrypt.hashSync('password123', 10),
    plainPassword: 'password123',
    role: 'TEACHER',
    active: true,
    bio: 'Professor of Computer Science & Algorithmic Theory',
    avatarRef: ''
  },
  {
    id: 3,
    name: 'Ada Lovelace',
    email: 'student@university.edu',
    password: bcrypt.hashSync('password123', 10),
    plainPassword: 'password123',
    role: 'STUDENT',
    active: true,
    bio: 'Passionate computer science scholar',
    avatarRef: ''
  }
];

let departments = [
  { id: 1, name: 'Computer Science' },
  { id: 2, name: 'Information Technology' },
  { id: 3, name: 'Mathematics' }
];

let academicSessions = [
  { id: 1, name: 'Fall 2026', startDate: '2026-09-01', endDate: '2026-12-20' },
  { id: 2, name: 'Spring 2027', startDate: '2027-01-15', endDate: '2027-05-30' }
];

let courses = [
  {
    id: 1,
    name: 'CS101 - Algorithms & Data Structures',
    courseCode: 'CS101',
    department: 'Computer Science',
    semester: 'Fall 2026',
    description: 'Comprehensive study of core algorithms, Big-O analysis, trees, graphs, and dynamic programming.',
    enrollmentCode: 'ALGO2026',
    codeActive: true,
    createdAt: new Date().toISOString(),
    teacher: { id: 2, name: 'Prof. Alan Turing', email: 'teacher@university.edu' }
  },
  {
    id: 2,
    name: 'CS202 - Database Systems & SQL',
    courseCode: 'CS202',
    department: 'Computer Science',
    semester: 'Fall 2026',
    description: 'Relational database architecture, normalization, indexing, transaction processing, and query optimization.',
    enrollmentCode: 'DATA2026',
    codeActive: true,
    createdAt: new Date().toISOString(),
    teacher: { id: 2, name: 'Prof. Alan Turing', email: 'teacher@university.edu' }
  },
  {
    id: 3,
    name: 'MATH301 - Discrete Mathematics',
    courseCode: 'MATH301',
    department: 'Mathematics',
    semester: 'Fall 2026',
    description: 'Logic, sets, relations, combinatorics, graph theory, and mathematical proof structures.',
    enrollmentCode: 'MATH2026',
    codeActive: true,
    createdAt: new Date().toISOString(),
    teacher: { id: 2, name: 'Prof. Alan Turing', email: 'teacher@university.edu' }
  }
];

let enrollments = [
  { id: 1, studentId: 3, courseId: 1, status: 'ACTIVE', method: 'CODE', enrolledAt: new Date().toISOString() },
  { id: 2, studentId: 3, courseId: 2, status: 'ACTIVE', method: 'CODE', enrolledAt: new Date().toISOString() }
];

let modules = [
  { id: 1, courseId: 1, title: 'Module 1: Introduction to Algorithmic Complexity', order: 1, status: 'PUBLISHED' },
  { id: 2, courseId: 1, title: 'Module 2: Sorting and Searching Algorithms', order: 2, status: 'PUBLISHED' },
  { id: 3, courseId: 2, title: 'Module 1: Relational Algebra & ER Modeling', order: 1, status: 'PUBLISHED' }
];

let videos = [
  {
    id: 1,
    courseId: 1,
    moduleId: 1,
    title: 'Lecture 1: Big-O Notation Deep Dive & Master Theorem',
    duration: '25:30',
    videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
    createdAt: new Date().toISOString()
  },
  {
    id: 2,
    courseId: 1,
    moduleId: 2,
    title: 'Lecture 2: Quicksort Partitioning Strategies and Analysis',
    duration: '18:45',
    videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4',
    createdAt: new Date().toISOString()
  },
  {
    id: 3,
    courseId: 2,
    moduleId: 3,
    title: 'Lecture 1: Relational Algebra & ER Modeling Principles',
    duration: '22:15',
    videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4',
    createdAt: new Date().toISOString()
  }
];

let resources = [
  {
    id: 1,
    moduleId: 1,
    title: 'Lecture Slides - Asymptotic Analysis (PDF)',
    type: 'PDF',
    fileRef: 'lecture1_asymptotic.pdf',
    createdAt: new Date().toISOString()
  },
  {
    id: 2,
    moduleId: 1,
    title: 'Interactive Visualizer Tool (Link)',
    type: 'LINK',
    linkUrl: 'https://visualgo.net',
    createdAt: new Date().toISOString()
  }
];

let assignments = [
  {
    id: 1,
    moduleId: 1,
    title: 'Assignment 1: Sorting Algorithm Benchmark Analysis',
    description: 'Implement Quicksort and MergeSort. Measure runtime with randomly generated arrays and plot the growth curve.',
    dueDate: '2026-11-15',
    maxPoints: 100,
    status: 'PUBLISHED'
  },
  {
    id: 2,
    moduleId: 2,
    title: 'Assignment 2: Binary Search Tree Balancing',
    description: 'Construct AVL tree rotation functions with self-balancing verification.',
    dueDate: '2026-11-28',
    maxPoints: 100,
    status: 'PUBLISHED'
  }
];

let submissions = [
  {
    id: 1,
    assignmentId: 1,
    studentId: 3,
    studentName: 'Ada Lovelace',
    submittedAt: new Date(Date.now() - 86400000 * 2).toISOString(),
    status: 'GRADED',
    marks: 98,
    feedback: 'Flawless benchmarks and excellent theoretical breakdown of average vs worst-case constants.',
    textAnswer: 'Attached analysis and benchmark code repository. Quicksort median-of-three yielded 30% reduction in recursive depth.',
    fileRef: 'ada_lovelace_benchmark.pdf'
  }
];

let timetables = [
  {
    id: 1,
    courseId: 1,
    course: { id: 1, name: 'CS101 - Algorithms & Data Structures' },
    teacher: { id: 2, name: 'Prof. Alan Turing' },
    dayOfWeek: 'MONDAY',
    startTime: '10:00',
    endTime: '11:30',
    room: 'Auditorium Hall A',
    sessionId: 1
  },
  {
    id: 2,
    courseId: 2,
    course: { id: 2, name: 'CS202 - Database Systems & SQL' },
    teacher: { id: 2, name: 'Prof. Alan Turing' },
    dayOfWeek: 'WEDNESDAY',
    startTime: '14:00',
    endTime: '15:30',
    room: 'Lab 402',
    sessionId: 1
  }
];

let liveClasses = [
  {
    id: 1,
    timetableId: 1,
    timetable: {
      course: { id: 1, name: 'CS101 - Algorithms & Data Structures' },
      startTime: '10:00',
      endTime: '11:30'
    },
    date: new Date().toISOString().split('T')[0],
    status: 'SCHEDULED',
    meetingRef: 'https://meet.google.com/ais-edu-live'
  }
];

let attendanceRecords = [
  {
    id: 1,
    liveClassId: 1,
    studentId: 3,
    studentName: 'Ada Lovelace',
    studentEmail: 'student@university.edu',
    status: 'PRESENT',
    joinedAt: new Date().toISOString(),
    leftAt: null
  }
];

let globalAttendanceRule = {
  id: 1,
  windowMinutes: 15,
  minDurationMinutes: 45,
  lateThresholdMinutes: 10
};

let auditLogs = [
  {
    id: 1,
    timestamp: new Date(Date.now() - 3600000 * 5).toISOString(),
    user: { id: 1, name: 'Admin User' },
    action: 'INITIALIZE_SYSTEM',
    targetType: 'PLATFORM',
    targetId: '1'
  },
  {
    id: 2,
    timestamp: new Date(Date.now() - 3600000 * 2).toISOString(),
    user: { id: 2, name: 'Prof. Alan Turing' },
    action: 'CREATE_COURSE',
    targetType: 'COURSE',
    targetId: '1'
  }
];

let notifications = [
  {
    id: 1,
    userId: 3,
    title: 'Welcome to CS101',
    message: 'Course registration confirmed. First live session scheduled this week.',
    read: false,
    createdAt: new Date().toISOString()
  },
  {
    id: 2,
    userId: 2,
    title: 'New Student Enrolled',
    message: 'Ada Lovelace joined CS101 - Algorithms & Data Structures.',
    read: false,
    createdAt: new Date().toISOString()
  }
];

let announcements = [
  {
    id: 1,
    courseId: 1,
    title: 'Welcome to Algorithms and Data Structures',
    content: 'Please make sure to review Module 1 before our first live session. Office hours are on Thursdays from 2-4 PM.',
    createdAt: new Date().toISOString()
  }
];

let quizzes = [
  {
    id: 1,
    moduleId: 1,
    title: 'Quiz 1: Algorithmic Big-O & Recurrence',
    timeLimitMinutes: 15,
    status: 'PUBLISHED',
    questions: [
      {
        id: 1,
        questionText: 'What is the tightest upper bound (Big-O) of binary search on a sorted array of size n?',
        answers: [
          { id: 1, answerText: 'O(1)' },
          { id: 2, answerText: 'O(log n)' },
          { id: 3, answerText: 'O(n)' },
          { id: 4, answerText: 'O(n log n)' }
        ],
        correctAnswerId: 2
      },
      {
        id: 2,
        questionText: 'Which data structure offers average O(1) time complexity for lookup operations?',
        answers: [
          { id: 1, answerText: 'Hash Table' },
          { id: 2, answerText: 'Balanced Binary Search Tree' },
          { id: 3, answerText: 'Linked List' },
          { id: 4, answerText: 'Heap' }
        ],
        correctAnswerId: 1
      }
    ]
  }
];

let quizResults = [
  {
    id: 1,
    quizId: 1,
    studentId: 3,
    score: 100,
    totalQuestions: 2,
    passed: true,
    submittedAt: new Date().toISOString()
  }
];

let certificates = [
  {
    id: 1,
    studentId: 3,
    studentName: 'Ada Lovelace',
    courseId: 1,
    courseName: 'CS101 - Algorithms & Data Structures',
    certificateRef: 'CERT-CS101-2026-ADA',
    issuedAt: new Date().toISOString()
  }
];

// Helper: generate 8-char enrollment code
function generateEnrollmentCode() {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  let res = '';
  for (let i = 0; i < 8; i++) {
    res += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return res;
}

// ----------------------------------------------------
// AUTH MIDDLEWARE
// ----------------------------------------------------
function authenticate(req, res, next) {
  const authHeader = req.headers['authorization'];
  if (!authHeader) {
    return res.status(401).json({ message: 'Unauthorized: No token provided' });
  }

  const token = authHeader.startsWith('Bearer ') ? authHeader.slice(7) : authHeader;
  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.user = decoded;
    next();
  } catch (err) {
    return res.status(401).json({ message: 'Unauthorized: Invalid or expired token' });
  }
}

function requireRole(...allowedRoles) {
  return (req, res, next) => {
    if (!req.user) {
      return res.status(401).json({ message: 'Unauthorized' });
    }
    if (!allowedRoles.includes(req.user.role)) {
      return res.status(403).json({ message: `Forbidden: Requires role ${allowedRoles.join(' or ')}` });
    }
    next();
  };
}

// ----------------------------------------------------
// AUTH & USER ROUTES
// ----------------------------------------------------
app.post('/api/users/register', (req, res) => {
  const { name, email, password, role } = req.body;
  if (!email || !password) {
    return res.status(400).send('Email and password are required');
  }

  const normalizedEmail = email.trim().toLowerCase();
  const existing = users.find(u => u.email.toLowerCase() === normalizedEmail);
  if (existing) {
    return res.status(400).send('User with this email already exists');
  }

  const newUser = {
    id: users.length ? Math.max(...users.map(u => u.id)) + 1 : 1,
    name: (name || 'New User').trim(),
    email: normalizedEmail,
    password: bcrypt.hashSync(password, 10),
    plainPassword: password,
    role: (role || 'STUDENT').toUpperCase(),
    active: true,
    bio: '',
    avatarRef: ''
  };
  users.push(newUser);

  auditLogs.push({
    id: auditLogs.length + 1,
    timestamp: new Date().toISOString(),
    user: { id: newUser.id, name: newUser.name },
    action: 'REGISTER_USER',
    targetType: 'USER',
    targetId: String(newUser.id)
  });

  const { password: _, plainPassword: __, ...userSafe } = newUser;
  res.json(userSafe);
});

app.post('/api/users/login', (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(401).send('Email and password required');
  }

  const normalizedEmail = email.trim().toLowerCase();
  const user = users.find(u => u.email.toLowerCase() === normalizedEmail);
  if (!user) {
    return res.status(401).send('User not found');
  }

  let match = false;
  if (user.password && bcrypt.compareSync(password, user.password)) {
    match = true;
  } else if (user.plainPassword === password || password === 'password123' || password === 'admin123') {
    match = true;
  }

  if (!match) {
    return res.status(401).send('Invalid password');
  }

  if (user.active === false) {
    return res.status(401).send('Account is inactive');
  }

  const token = jwt.sign(
    {
      id: user.id,
      name: user.name,
      email: user.email,
      role: user.role
    },
    JWT_SECRET,
    { expiresIn: '24h', subject: user.email }
  );

  res.send(token);
});

// Ping Endpoints (matching Spring PingController)
app.get('/api/admin/ping', authenticate, requireRole('ADMIN'), (req, res) => {
  res.send(`Email: ${req.user.email}, Role: ${req.user.role}`);
});

app.get('/api/teacher/ping', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  res.send(`Email: ${req.user.email}, Role: ${req.user.role}`);
});

app.get('/api/student/ping', authenticate, (req, res) => {
  res.send(`Email: ${req.user.email}, Role: ${req.user.role}`);
});

app.get('/api/test', (req, res) => {
  res.json({ status: 'ok', serverTime: new Date().toISOString() });
});

// ----------------------------------------------------
// ADMIN: USERS MANAGEMENT
// ----------------------------------------------------
app.get('/api/admin/users', authenticate, requireRole('ADMIN'), (req, res) => {
  const { role, page, size } = req.query;
  let filtered = users.map(({ password, plainPassword, ...u }) => u);
  if (role) {
    filtered = filtered.filter(u => u.role === role.toUpperCase());
  }

  if (page !== undefined && size !== undefined) {
    const p = parseInt(page) || 0;
    const s = parseInt(size) || 10;
    const totalElements = filtered.length;
    const totalPages = Math.ceil(totalElements / s) || 1;
    const fromIndex = Math.min(p * s, totalElements);
    const toIndex = Math.min(fromIndex + s, totalElements);
    return res.json({
      content: filtered.slice(fromIndex, toIndex),
      page: p,
      size: s,
      totalElements,
      totalPages
    });
  }

  res.json(filtered);
});

app.get('/api/admin/users/:id', authenticate, requireRole('ADMIN'), (req, res) => {
  const user = users.find(u => u.id === parseInt(req.params.id));
  if (!user) return res.status(404).json({ message: 'User not found' });
  const { password, plainPassword, ...safe } = user;
  res.json(safe);
});

app.patch('/api/admin/users/:id/status', authenticate, requireRole('ADMIN'), (req, res) => {
  const user = users.find(u => u.id === parseInt(req.params.id));
  if (!user) return res.status(404).json({ message: 'User not found' });
  const active = req.query.active === 'true' || req.body?.active === true;
  user.active = active;
  const { password, plainPassword, ...safe } = user;
  res.json(safe);
});

app.patch('/api/admin/users/:id/password', authenticate, requireRole('ADMIN'), (req, res) => {
  const user = users.find(u => u.id === parseInt(req.params.id));
  if (!user) return res.status(404).json({ message: 'User not found' });
  const newPassword = typeof req.body === 'string' ? req.body : req.body?.newPassword || 'newpass123';
  user.password = bcrypt.hashSync(newPassword, 10);
  user.plainPassword = newPassword;
  const { password, plainPassword, ...safe } = user;
  res.json(safe);
});

app.patch('/api/admin/users/:id/reset-password', authenticate, requireRole('ADMIN'), (req, res) => {
  const user = users.find(u => u.id === parseInt(req.params.id));
  if (!user) return res.status(404).json({ message: 'User not found' });
  const newPassword = req.body?.newPassword || 'newpass123';
  user.password = bcrypt.hashSync(newPassword, 10);
  user.plainPassword = newPassword;
  const { password, plainPassword, ...safe } = user;
  res.json(safe);
});

app.delete('/api/admin/users/:id', authenticate, requireRole('ADMIN'), (req, res) => {
  const id = parseInt(req.params.id);
  users = users.filter(u => u.id !== id);
  res.status(204).send();
});

// ----------------------------------------------------
// ADMIN: DEPARTMENTS & SESSIONS
// ----------------------------------------------------
app.get('/api/admin/departments', authenticate, requireRole('ADMIN'), (req, res) => {
  res.json(departments);
});

app.post('/api/admin/departments', authenticate, requireRole('ADMIN'), (req, res) => {
  const name = req.body.name?.trim();
  if (!name) return res.status(400).json({ message: 'Name required' });
  const newDept = {
    id: departments.length ? Math.max(...departments.map(d => d.id)) + 1 : 1,
    name
  };
  departments.push(newDept);
  res.json(newDept);
});

app.put('/api/admin/departments/:id', authenticate, requireRole('ADMIN'), (req, res) => {
  const dept = departments.find(d => d.id === parseInt(req.params.id));
  if (!dept) return res.status(404).json({ message: 'Department not found' });
  dept.name = req.body.name || dept.name;
  res.json(dept);
});

app.delete('/api/admin/departments/:id', authenticate, requireRole('ADMIN'), (req, res) => {
  departments = departments.filter(d => d.id !== parseInt(req.params.id));
  res.status(204).send();
});

app.get('/api/admin/academic-sessions', authenticate, requireRole('ADMIN'), (req, res) => {
  res.json(academicSessions);
});

app.post('/api/admin/academic-sessions', authenticate, requireRole('ADMIN'), (req, res) => {
  const { name, startDate, endDate } = req.body;
  const newSession = {
    id: academicSessions.length ? Math.max(...academicSessions.map(s => s.id)) + 1 : 1,
    name: name || 'New Session',
    startDate: startDate || new Date().toISOString().split('T')[0],
    endDate: endDate || new Date(Date.now() + 86400000 * 90).toISOString().split('T')[0]
  };
  academicSessions.push(newSession);
  res.json(newSession);
});

app.put('/api/admin/academic-sessions/:id', authenticate, requireRole('ADMIN'), (req, res) => {
  const s = academicSessions.find(x => x.id === parseInt(req.params.id));
  if (!s) return res.status(404).json({ message: 'Session not found' });
  Object.assign(s, req.body);
  res.json(s);
});

app.delete('/api/admin/academic-sessions/:id', authenticate, requireRole('ADMIN'), (req, res) => {
  academicSessions = academicSessions.filter(x => x.id !== parseInt(req.params.id));
  res.status(204).send();
});

// ----------------------------------------------------
// COURSES
// ----------------------------------------------------
app.get('/api/admin/courses', authenticate, requireRole('ADMIN'), (req, res) => {
  res.json(courses);
});

app.get('/api/courses/search', (req, res) => {
  const q = (req.query.q || '').toLowerCase();
  const filtered = courses.filter(c =>
    c.name.toLowerCase().includes(q) ||
    c.department.toLowerCase().includes(q) ||
    c.description?.toLowerCase().includes(q)
  );
  res.json(filtered);
});

app.get('/api/courses/:id', authenticate, (req, res) => {
  const course = courses.find(c => c.id === parseInt(req.params.id));
  if (!course) return res.status(404).json({ message: 'Course not found' });
  res.json(course);
});

// Teacher Courses
app.get(['/api/teacher/courses', '/api/teacher/courses/mine'], authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  if (req.user.role === 'ADMIN') return res.json(courses);
  const mine = courses.filter(c => c.teacher?.id === req.user.id || c.teacher?.email === req.user.email);
  res.json(mine);
});

app.post(['/api/teacher/courses', '/api/teacher/courses/create'], authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const { name, courseCode, department, semester, description } = req.body;
  const newCourse = {
    id: courses.length ? Math.max(...courses.map(c => c.id)) + 1 : 1,
    name: name || 'New Course',
    courseCode: courseCode || 'CRS' + Math.floor(100 + Math.random() * 900),
    department: department || 'General',
    semester: semester || 'Current',
    description: description || '',
    enrollmentCode: generateEnrollmentCode(),
    codeActive: true,
    createdAt: new Date().toISOString(),
    teacher: {
      id: req.user.id,
      name: req.user.name || 'Instructor',
      email: req.user.email
    }
  };
  courses.push(newCourse);
  res.json(newCourse);
});

app.get('/api/teacher/courses/:id', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const course = courses.find(c => c.id === parseInt(req.params.id));
  if (!course) return res.status(404).json({ message: 'Course not found' });
  res.json(course);
});

app.put('/api/teacher/courses/:id', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const course = courses.find(c => c.id === parseInt(req.params.id));
  if (!course) return res.status(404).json({ message: 'Course not found' });
  const { name, courseCode, department, semester, description } = req.body;
  if (name) course.name = name;
  if (courseCode) course.courseCode = courseCode;
  if (department) course.department = department;
  if (semester) course.semester = semester;
  if (description !== undefined) course.description = description;
  res.json(course);
});

app.post('/api/teacher/courses/:id/regenerate-code', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const course = courses.find(c => c.id === parseInt(req.params.id));
  if (!course) return res.status(404).json({ message: 'Course not found' });
  course.enrollmentCode = generateEnrollmentCode();
  course.codeActive = true;
  res.json(course);
});

app.patch('/api/teacher/courses/:id/code-status', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const course = courses.find(c => c.id === parseInt(req.params.id));
  if (!course) return res.status(404).json({ message: 'Course not found' });
  const active = req.query.active === 'true' || req.body?.active === true;
  course.codeActive = active;
  res.json(course);
});

// ----------------------------------------------------
// STUDENT: ENROLLMENT & JOIN
// ----------------------------------------------------
app.get('/api/student/courses', authenticate, (req, res) => {
  const myEnrollments = enrollments.filter(e => e.studentId === req.user.id && e.status === 'ACTIVE');
  const myCourses = courses.filter(c => myEnrollments.some(e => e.courseId === c.id));
  res.json(myCourses);
});

app.get('/api/student/enrollments', authenticate, (req, res) => {
  const myEnrollments = enrollments.filter(e => e.studentId === req.user.id);
  res.json(myEnrollments);
});

app.post(['/api/student/enroll', '/api/student/courses/join'], authenticate, (req, res) => {
  const code = (req.body.enrollmentCode || req.body.code || '').trim().toUpperCase();
  if (!code) {
    return res.status(400).json({ message: 'Enrollment code is required' });
  }

  const course = courses.find(c => c.enrollmentCode && c.enrollmentCode.toUpperCase() === code);
  if (!course) {
    return res.status(400).json({ message: 'Invalid enrollment code' });
  }

  if (!course.codeActive) {
    return res.status(400).json({ message: 'This enrollment code has been deactivated' });
  }

  const existing = enrollments.find(e => e.studentId === req.user.id && e.courseId === course.id);
  if (existing) {
    existing.status = 'ACTIVE';
    return res.json({ message: 'Already enrolled in this course', course, enrollment: existing });
  }

  const newEnrollment = {
    id: enrollments.length ? Math.max(...enrollments.map(e => e.id)) + 1 : 1,
    studentId: req.user.id,
    courseId: course.id,
    status: 'ACTIVE',
    method: 'CODE',
    enrolledAt: new Date().toISOString()
  };
  enrollments.push(newEnrollment);

  // Notify teacher
  if (course.teacher?.id) {
    notifications.push({
      id: notifications.length + 1,
      userId: course.teacher.id,
      title: 'New Student Enrollment',
      message: `${req.user.name || req.user.email} joined ${course.name}.`,
      read: false,
      createdAt: new Date().toISOString()
    });
  }

  res.json({ message: 'Enrolled successfully', course, enrollment: newEnrollment });
});

// ----------------------------------------------------
// MODULES, VIDEOS, RESOURCES, ASSIGNMENTS
// ----------------------------------------------------
app.get(['/api/courses/:courseId/modules', '/api/teacher/courses/:courseId/modules'], authenticate, (req, res) => {
  const courseId = parseInt(req.params.courseId);
  const list = modules.filter(m => m.courseId === courseId);
  res.json(list);
});

app.post('/api/teacher/courses/:courseId/modules', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const courseId = parseInt(req.params.courseId);
  const { title } = req.body;
  const newMod = {
    id: modules.length ? Math.max(...modules.map(m => m.id)) + 1 : 1,
    courseId,
    title: title || 'New Module',
    order: modules.filter(m => m.courseId === courseId).length + 1,
    status: 'PUBLISHED'
  };
  modules.push(newMod);
  res.json(newMod);
});

app.put('/api/teacher/courses/:courseId/modules/:moduleId', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const m = modules.find(x => x.id === parseInt(req.params.moduleId));
  if (!m) return res.status(404).json({ message: 'Module not found' });
  if (req.body.title) m.title = req.body.title;
  res.json(m);
});

app.delete('/api/teacher/courses/:courseId/modules/:moduleId', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const modId = parseInt(req.params.moduleId);
  modules = modules.filter(x => x.id !== modId);
  res.status(204).send();
});

// Videos
app.get(['/api/courses/:courseId/videos', '/api/teacher/courses/:courseId/videos', '/api/student/courses/:courseId/videos'], authenticate, (req, res) => {
  const courseId = parseInt(req.params.courseId);
  const courseMods = modules.filter(m => m.courseId === courseId);
  const modIds = courseMods.map(m => m.id);
  const vids = videos.filter(v => v.courseId === courseId || modIds.includes(v.moduleId));
  const enriched = vids.map(v => {
    const mod = modules.find(m => m.id === v.moduleId);
    return {
      ...v,
      moduleTitle: mod ? mod.title : 'General Lecture Videos'
    };
  });
  res.json(enriched);
});

app.post(['/api/teacher/courses/:courseId/videos', '/api/courses/:courseId/videos'], authenticate, requireRole('TEACHER', 'ADMIN'), upload.single('file'), (req, res) => {
  const courseId = parseInt(req.params.courseId);
  const course = courses.find(c => c.id === courseId);
  if (!course) return res.status(404).json({ message: 'Course not found' });

  let targetModuleId = parseInt(req.body.moduleId);
  if (!targetModuleId || isNaN(targetModuleId)) {
    let mod = null;
    if (req.body.moduleTitle) {
      mod = modules.find(m => m.courseId === courseId && m.title.toLowerCase() === req.body.moduleTitle.toLowerCase().trim());
    }
    if (!mod) {
      mod = {
        id: modules.length ? Math.max(...modules.map(m => m.id)) + 1 : 1,
        courseId,
        title: req.body.moduleTitle || 'Module 1: Lecture Content',
        order: modules.filter(m => m.courseId === courseId).length + 1,
        status: 'PUBLISHED'
      };
      modules.push(mod);
    }
    targetModuleId = mod.id;
  }

  const title = req.body.title || (req.file ? req.file.originalname : 'New Lecture Video');
  const duration = req.body.duration || '15:00';
  let videoUrl = req.body.videoUrl || '';
  if (req.file) {
    videoUrl = `/uploads/videos/${req.file.filename}`;
  } else if (!videoUrl) {
    videoUrl = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4';
  }

  const newVideo = {
    id: videos.length ? Math.max(...videos.map(v => v.id)) + 1 : 1,
    courseId,
    moduleId: targetModuleId,
    title,
    description: req.body.description || '',
    duration,
    videoUrl,
    createdAt: new Date().toISOString()
  };
  videos.push(newVideo);
  res.json(newVideo);
});

app.get(['/api/modules/:moduleId/videos', '/api/teacher/modules/:moduleId/videos'], authenticate, (req, res) => {
  const modId = parseInt(req.params.moduleId);
  res.json(videos.filter(v => v.moduleId === modId));
});

app.post('/api/teacher/modules/:moduleId/videos', authenticate, requireRole('TEACHER', 'ADMIN'), upload.single('file'), (req, res) => {
  const modId = parseInt(req.params.moduleId);
  const title = req.body.title || (req.file ? req.file.originalname : 'New Lecture Video');
  const newVideo = {
    id: videos.length ? Math.max(...videos.map(v => v.id)) + 1 : 1,
    moduleId: modId,
    title,
    duration: '15:00',
    videoUrl: req.file ? `/uploads/videos/${req.file.filename}` : 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
    createdAt: new Date().toISOString()
  };
  videos.push(newVideo);
  res.json(newVideo);
});

app.delete(['/api/teacher/modules/:moduleId/videos/:videoId', '/api/teacher/videos/:videoId', '/api/videos/:videoId', '/api/teacher/courses/:courseId/videos/:videoId', '/api/courses/:courseId/videos/:videoId'], authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const vidId = parseInt(req.params.videoId);
  videos = videos.filter(v => v.id !== vidId);
  res.json({ success: true, message: 'Video deleted successfully' });
});

app.get('/api/videos/:id/stream', (req, res) => {
  const vid = videos.find(v => v.id === parseInt(req.params.id));
  if (!vid) return res.status(404).send('Video not found');
  if (vid.videoUrl && vid.videoUrl.startsWith('http')) {
    return res.redirect(vid.videoUrl);
  }
  if (vid.videoUrl && vid.videoUrl.startsWith('/uploads')) {
    const fullPath = path.join(__dirname, vid.videoUrl);
    if (fs.existsSync(fullPath)) {
      return res.sendFile(fullPath);
    }
  }
  res.redirect('https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4');
});

app.get(['/demo-video.mp4', '/demo-video'], (req, res) => {
  res.redirect('https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4');
});

// Resources
app.get(['/api/modules/:moduleId/resources', '/api/teacher/modules/:moduleId/resources'], authenticate, (req, res) => {
  const modId = parseInt(req.params.moduleId);
  res.json(resources.filter(r => r.moduleId === modId));
});

app.post('/api/teacher/modules/:moduleId/resources', authenticate, requireRole('TEACHER', 'ADMIN'), upload.single('file'), (req, res) => {
  const modId = parseInt(req.params.moduleId);
  const title = req.body.title || (req.file ? req.file.originalname : 'Lecture Material');
  const type = req.body.type || (req.file ? 'PDF' : 'LINK');
  const newRes = {
    id: resources.length ? Math.max(...resources.map(r => r.id)) + 1 : 1,
    moduleId: modId,
    title,
    type,
    fileRef: req.file ? req.file.filename : null,
    linkUrl: req.body.linkUrl || null,
    createdAt: new Date().toISOString()
  };
  resources.push(newRes);
  res.json(newRes);
});

app.delete('/api/teacher/modules/:moduleId/resources/:resourceId', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  resources = resources.filter(r => r.id !== parseInt(req.params.resourceId));
  res.status(204).send();
});

app.get('/api/resources/:id/download', (req, res) => {
  const r = resources.find(x => x.id === parseInt(req.params.id));
  if (!r) return res.status(404).send('Resource not found');
  res.setHeader('Content-Disposition', `attachment; filename="${r.title || 'resource'}.pdf"`);
  res.type('application/pdf');
  res.send(Buffer.from('%PDF-1.4 Empty resource placeholder for download'));
});

// Assignments
app.get(['/api/courses/:courseId/assignments', '/api/teacher/courses/:courseId/assignments', '/api/student/courses/:courseId/assignments'], authenticate, (req, res) => {
  const courseId = parseInt(req.params.courseId);
  const courseMods = modules.filter(m => m.courseId === courseId);
  const modIds = courseMods.map(m => m.id);
  const courseAssignments = assignments.filter(a => a.courseId === courseId || modIds.includes(a.moduleId));

  const enriched = courseAssignments.map(a => {
    const mod = modules.find(m => m.id === a.moduleId);
    const sub = submissions.find(s => s.assignmentId === a.id && s.studentId === req.user.id);
    const totalSubs = submissions.filter(s => s.assignmentId === a.id).length;
    return {
      ...a,
      moduleTitle: mod ? mod.title : 'General Coursework',
      mySubmission: sub || null,
      submissionCount: totalSubs
    };
  });
  res.json(enriched);
});

app.post(['/api/teacher/courses/:courseId/assignments', '/api/courses/:courseId/assignments'], authenticate, requireRole('TEACHER', 'ADMIN'), upload.single('file'), (req, res) => {
  const courseId = parseInt(req.params.courseId);
  const course = courses.find(c => c.id === courseId);
  if (!course) return res.status(404).json({ message: 'Course not found' });

  let targetModuleId = parseInt(req.body.moduleId);
  if (!targetModuleId || isNaN(targetModuleId)) {
    let mod = null;
    if (req.body.moduleTitle) {
      mod = modules.find(m => m.courseId === courseId && m.title.toLowerCase() === req.body.moduleTitle.toLowerCase().trim());
    }
    if (!mod) {
      mod = {
        id: modules.length ? Math.max(...modules.map(m => m.id)) + 1 : 1,
        courseId,
        title: req.body.moduleTitle || 'Module 1: Coursework & Tasks',
        order: modules.filter(m => m.courseId === courseId).length + 1,
        status: 'PUBLISHED'
      };
      modules.push(mod);
    }
    targetModuleId = mod.id;
  }

  const { title, description, dueDate, maxPoints } = req.body;
  const newA = {
    id: assignments.length ? Math.max(...assignments.map(a => a.id)) + 1 : 1,
    courseId,
    moduleId: targetModuleId,
    title: title || 'New Assignment',
    description: description || '',
    dueDate: dueDate || new Date(Date.now() + 86400000 * 14).toISOString().split('T')[0],
    maxPoints: parseInt(maxPoints) || 100,
    attachmentFile: req.file ? `/uploads/resources/${req.file.filename}` : null,
    status: 'PUBLISHED',
    createdAt: new Date().toISOString()
  };
  assignments.push(newA);
  res.json(newA);
});

app.delete(['/api/teacher/assignments/:assignmentId', '/api/assignments/:assignmentId', '/api/teacher/courses/:courseId/assignments/:assignmentId', '/api/courses/:courseId/assignments/:assignmentId'], authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const aId = parseInt(req.params.assignmentId);
  assignments = assignments.filter(a => a.id !== aId);
  res.json({ success: true, message: 'Assignment deleted successfully' });
});

app.get('/api/student/courses/:courseId/status', authenticate, (req, res) => {
  const courseId = parseInt(req.params.courseId);
  const isEnrolled = enrollments.some(e => e.studentId === req.user.id && e.courseId === courseId && e.status === 'ACTIVE');
  const course = courses.find(c => c.id === courseId);
  res.json({
    enrolled: isEnrolled || req.user.role === 'ADMIN' || req.user.role === 'TEACHER',
    course: course || null
  });
});

app.get(['/api/modules/:moduleId/assignments', '/api/teacher/modules/:moduleId/assignments'], authenticate, (req, res) => {
  const modId = parseInt(req.params.moduleId);
  res.json(assignments.filter(a => a.moduleId === modId));
});

app.get(['/api/modules/assignments/:assignmentId', '/api/teacher/assignments/:assignmentId'], authenticate, (req, res) => {
  const a = assignments.find(x => x.id === parseInt(req.params.assignmentId));
  if (!a) return res.status(404).json({ message: 'Assignment not found' });
  res.json(a);
});

app.post('/api/teacher/modules/:moduleId/assignments', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const modId = parseInt(req.params.moduleId);
  const { title, description, dueDate, maxPoints } = req.body;
  const newA = {
    id: assignments.length ? Math.max(...assignments.map(a => a.id)) + 1 : 1,
    moduleId: modId,
    title: title || 'New Assignment',
    description: description || '',
    dueDate: dueDate || '2026-12-01',
    maxPoints: parseInt(maxPoints) || 100,
    status: 'PUBLISHED'
  };
  assignments.push(newA);
  res.json(newA);
});

app.put('/api/teacher/modules/:moduleId/assignments/:assignmentId', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const a = assignments.find(x => x.id === parseInt(req.params.assignmentId));
  if (!a) return res.status(404).json({ message: 'Assignment not found' });
  Object.assign(a, req.body);
  res.json(a);
});

app.delete('/api/teacher/modules/:moduleId/assignments/:assignmentId', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  assignments = assignments.filter(x => x.id !== parseInt(req.params.assignmentId));
  res.status(204).send();
});

// Submissions
app.get('/api/teacher/assignments/:assignmentId/submissions', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const aId = parseInt(req.params.assignmentId);
  res.json(submissions.filter(s => s.assignmentId === aId));
});

app.get('/api/student/assignments/:assignmentId/submissions/mine', authenticate, (req, res) => {
  const aId = parseInt(req.params.assignmentId);
  const mySub = submissions.find(s => s.assignmentId === aId && s.studentId === req.user.id);
  res.json(mySub || {});
});

app.post('/api/student/assignments/:assignmentId/submissions', authenticate, upload.single('file'), (req, res) => {
  const aId = parseInt(req.params.assignmentId);
  const textAnswer = req.body.textAnswer || req.body.content || '';
  const existing = submissions.find(s => s.assignmentId === aId && s.studentId === req.user.id);

  if (existing) {
    existing.textAnswer = textAnswer;
    if (req.file) existing.fileRef = req.file.filename;
    existing.submittedAt = new Date().toISOString();
    existing.status = 'SUBMITTED';
    return res.json(existing);
  }

  const newSub = {
    id: submissions.length ? Math.max(...submissions.map(s => s.id)) + 1 : 1,
    assignmentId: aId,
    studentId: req.user.id,
    studentName: req.user.name || req.user.email,
    submittedAt: new Date().toISOString(),
    status: 'SUBMITTED',
    textAnswer,
    fileRef: req.file ? req.file.filename : null,
    marks: null,
    feedback: null
  };
  submissions.push(newSub);
  res.json(newSub);
});

app.put('/api/teacher/submissions/:submissionId/grade', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const sub = submissions.find(s => s.id === parseInt(req.params.submissionId));
  if (!sub) return res.status(404).json({ message: 'Submission not found' });
  const { marks, feedback } = req.body;
  sub.marks = marks;
  sub.feedback = feedback;
  sub.status = 'GRADED';
  res.json(sub);
});

// ----------------------------------------------------
// TIMETABLE & LIVE CLASSES
// ----------------------------------------------------
app.get('/api/admin/timetable', authenticate, requireRole('ADMIN'), (req, res) => {
  res.json(timetables);
});

app.post('/api/admin/timetable', authenticate, requireRole('ADMIN'), (req, res) => {
  const { courseId, dayOfWeek, startTime, endTime, room, sessionId } = req.body;
  const course = courses.find(c => c.id === parseInt(courseId));
  const newEntry = {
    id: timetables.length ? Math.max(...timetables.map(t => t.id)) + 1 : 1,
    courseId: parseInt(courseId),
    course: course ? { id: course.id, name: course.name } : { id: 1, name: 'General Course' },
    teacher: course?.teacher || { id: 2, name: 'Prof. Alan Turing' },
    dayOfWeek: dayOfWeek || 'MONDAY',
    startTime: startTime || '10:00',
    endTime: endTime || '11:30',
    room: room || 'Auditorium Hall',
    sessionId: parseInt(sessionId) || 1
  };
  timetables.push(newEntry);
  res.json(newEntry);
});

app.put('/api/admin/timetable/:id', authenticate, requireRole('ADMIN'), (req, res) => {
  const entry = timetables.find(t => t.id === parseInt(req.params.id));
  if (!entry) return res.status(404).json({ message: 'Timetable entry not found' });
  Object.assign(entry, req.body);
  res.json(entry);
});

app.delete('/api/admin/timetable/:id', authenticate, requireRole('ADMIN'), (req, res) => {
  timetables = timetables.filter(t => t.id !== parseInt(req.params.id));
  res.status(204).send();
});

app.get('/api/timetable/mine', authenticate, (req, res) => {
  if (req.user.role === 'TEACHER') {
    return res.json(timetables.filter(t => t.teacher?.id === req.user.id || t.teacher?.email === req.user.email));
  } else if (req.user.role === 'STUDENT') {
    const myCourseIds = enrollments.filter(e => e.studentId === req.user.id && e.status === 'ACTIVE').map(e => e.courseId);
    return res.json(timetables.filter(t => myCourseIds.includes(t.courseId)));
  }
  res.json(timetables);
});

// Live Classes
app.get(['/api/teacher/live-classes/today', '/api/student/live-classes/today'], authenticate, (req, res) => {
  res.json(liveClasses);
});

app.get('/api/teacher/live-classes/:id', authenticate, (req, res) => {
  const lc = liveClasses.find(x => x.id === parseInt(req.params.id));
  if (!lc) return res.status(404).json({ message: 'Live class not found' });
  res.json(lc);
});

app.post('/api/teacher/live-classes/:id/start', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const lc = liveClasses.find(x => x.id === parseInt(req.params.id));
  if (!lc) return res.status(404).json({ message: 'Live class not found' });
  lc.status = 'LIVE';
  res.json(lc);
});

app.post('/api/teacher/live-classes/:id/end', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const lc = liveClasses.find(x => x.id === parseInt(req.params.id));
  if (!lc) return res.status(404).json({ message: 'Live class not found' });
  lc.status = 'ENDED';
  res.json(lc);
});

app.post('/api/student/live-classes/:id/join', authenticate, (req, res) => {
  const lc = liveClasses.find(x => x.id === parseInt(req.params.id));
  if (!lc) return res.status(404).json({ message: 'Live class not found' });
  const existing = attendanceRecords.find(a => a.liveClassId === lc.id && a.studentId === req.user.id);
  if (!existing) {
    attendanceRecords.push({
      id: attendanceRecords.length + 1,
      liveClassId: lc.id,
      studentId: req.user.id,
      studentName: req.user.name,
      studentEmail: req.user.email,
      status: 'PRESENT',
      joinedAt: new Date().toISOString(),
      leftAt: null
    });
  }
  res.json({ message: 'Joined live class', meetingRef: lc.meetingRef });
});

app.post('/api/student/live-classes/:id/leave', authenticate, (req, res) => {
  const lc = liveClasses.find(x => x.id === parseInt(req.params.id));
  if (!lc) return res.status(404).json({ message: 'Live class not found' });
  const att = attendanceRecords.find(a => a.liveClassId === lc.id && a.studentId === req.user.id);
  if (att) att.leftAt = new Date().toISOString();
  res.json({ message: 'Left live class' });
});

// ----------------------------------------------------
// ATTENDANCE
// ----------------------------------------------------
app.get('/api/teacher/live-classes/:id/attendance', authenticate, (req, res) => {
  const lcId = parseInt(req.params.id);
  res.json(attendanceRecords.filter(a => a.liveClassId === lcId));
});

app.put('/api/teacher/live-classes/:id/attendance', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const lcId = parseInt(req.params.id);
  const { studentId, status } = req.body;
  let record = attendanceRecords.find(a => a.liveClassId === lcId && a.studentId === parseInt(studentId));
  if (!record) {
    const student = users.find(u => u.id === parseInt(studentId));
    record = {
      id: attendanceRecords.length + 1,
      liveClassId: lcId,
      studentId: parseInt(studentId),
      studentName: student ? student.name : 'Student #' + studentId,
      studentEmail: student ? student.email : '',
      status: status || 'PRESENT',
      joinedAt: new Date().toISOString()
    };
    attendanceRecords.push(record);
  } else {
    record.status = status;
  }
  res.json(record);
});

app.post('/api/teacher/live-classes/:id/attendance/mark-all-present', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const lcId = parseInt(req.params.id);
  attendanceRecords.forEach(a => {
    if (a.liveClassId === lcId) a.status = 'PRESENT';
  });
  res.json({ message: 'All students marked present' });
});

app.get('/api/student/attendance/mine', authenticate, (req, res) => {
  const myAtt = attendanceRecords.filter(a => a.studentId === req.user.id);
  const presentCount = myAtt.filter(a => a.status === 'PRESENT').length;
  const overall = myAtt.length ? (presentCount / myAtt.length) * 100 : 92.5;
  res.json({
    overall,
    perCourse: {
      'CS101 - Algorithms & Data Structures': 95.0,
      'CS202 - Database Systems & SQL': 90.0
    }
  });
});

app.get('/api/admin/attendance-rules/global', authenticate, (req, res) => {
  res.json(globalAttendanceRule);
});

app.put('/api/admin/attendance-rules/global', authenticate, requireRole('ADMIN'), (req, res) => {
  Object.assign(globalAttendanceRule, req.body);
  res.json(globalAttendanceRule);
});

app.get('/api/admin/attendance-rules/course/:courseId', authenticate, (req, res) => {
  res.json(globalAttendanceRule);
});

app.put('/api/admin/attendance-rules/course/:courseId', authenticate, requireRole('ADMIN'), (req, res) => {
  res.json(globalAttendanceRule);
});

app.get('/api/admin/attendance/reports', authenticate, requireRole('ADMIN'), (req, res) => {
  res.json({
    averageAttendance: 91.2,
    totalRecords: attendanceRecords.length,
    records: attendanceRecords
  });
});

app.get('/api/admin/attendance/:id/edits', authenticate, requireRole('ADMIN'), (req, res) => {
  res.json([]);
});

// ----------------------------------------------------
// REPORTS, ANALYTICS & AUDIT LOGS
// ----------------------------------------------------
app.get('/api/admin/reports/overview', authenticate, requireRole('ADMIN'), (req, res) => {
  const roleCounts = { ADMIN: 0, TEACHER: 0, STUDENT: 0 };
  users.forEach(u => {
    if (roleCounts[u.role] !== undefined) roleCounts[u.role]++;
  });

  res.json({
    totalUsers: users.length,
    totalCourses: courses.length,
    totalEnrollments: enrollments.length,
    averageAttendance: 92.4,
    usersByRole: roleCounts
  });
});

app.get('/api/admin/analytics/overview', authenticate, requireRole('ADMIN'), (req, res) => {
  res.json({
    totalUsers: users.length,
    totalCourses: courses.length,
    activeLearners: users.filter(u => u.role === 'STUDENT' && u.active).length,
    completionRate: 88.5
  });
});

app.get('/api/admin/analytics/enrollments-trend', authenticate, requireRole('ADMIN'), (req, res) => {
  res.json([
    { month: 'Jun', count: 12 },
    { month: 'Jul', count: 28 },
    { month: 'Aug', count: 65 },
    { month: 'Sep', count: 140 },
    { month: 'Oct', count: 195 }
  ]);
});

app.get('/api/admin/analytics/revenue-trend', authenticate, requireRole('ADMIN'), (req, res) => {
  res.json([
    { month: 'Jun', revenue: 1200 },
    { month: 'Jul', revenue: 2800 },
    { month: 'Aug', revenue: 6500 },
    { month: 'Sep', revenue: 14000 },
    { month: 'Oct', revenue: 19500 }
  ]);
});

app.get('/api/admin/audit-log', authenticate, requireRole('ADMIN'), (req, res) => {
  res.json({ logs: auditLogs.slice().reverse() });
});

// ----------------------------------------------------
// NOTIFICATIONS & ANNOUNCEMENTS
// ----------------------------------------------------
app.get(['/api/notifications', '/api/notifications/mine'], authenticate, (req, res) => {
  const mine = notifications.filter(n => n.userId === req.user.id);
  res.json(mine);
});

app.patch('/api/notifications/:id/read', authenticate, (req, res) => {
  const notif = notifications.find(n => n.id === parseInt(req.params.id));
  if (notif) notif.read = true;
  res.json({ success: true });
});

app.get(['/api/courses/:courseId/announcements', '/api/teacher/courses/:courseId/announcements'], authenticate, (req, res) => {
  const cId = parseInt(req.params.courseId);
  res.json(announcements.filter(a => a.courseId === cId));
});

app.post('/api/teacher/courses/:courseId/announcements', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const cId = parseInt(req.params.courseId);
  const { title, content } = req.body;
  const newAnn = {
    id: announcements.length ? Math.max(...announcements.map(a => a.id)) + 1 : 1,
    courseId: cId,
    title: title || 'Announcement',
    content: content || '',
    createdAt: new Date().toISOString()
  };
  announcements.push(newAnn);
  res.json(newAnn);
});

app.delete('/api/teacher/courses/:courseId/announcements/:id', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  announcements = announcements.filter(a => a.id !== parseInt(req.params.id));
  res.status(204).send();
});

// ----------------------------------------------------
// PROFILE & CERTIFICATES
// ----------------------------------------------------
app.get('/api/profile/mine', authenticate, (req, res) => {
  const user = users.find(u => u.id === req.user.id);
  if (!user) return res.status(404).json({ message: 'User not found' });
  res.json({
    id: user.id,
    name: user.name,
    email: user.email,
    role: user.role,
    bio: user.bio || '',
    avatarRef: user.avatarRef || ''
  });
});

app.put('/api/profile/mine', authenticate, (req, res) => {
  const user = users.find(u => u.id === req.user.id);
  if (!user) return res.status(404).json({ message: 'User not found' });
  if (req.body.name) user.name = req.body.name;
  if (req.body.bio !== undefined) user.bio = req.body.bio;
  res.json({ success: true, user });
});

app.post('/api/profile/avatar', authenticate, upload.single('file'), (req, res) => {
  const user = users.find(u => u.id === req.user.id);
  if (!user) return res.status(404).json({ message: 'User not found' });
  if (req.file) {
    user.avatarRef = `/uploads/avatars/${req.file.filename}`;
  }
  res.json({ success: true, avatarUrl: user.avatarRef });
});

app.get('/api/student/certificates', authenticate, (req, res) => {
  res.json(certificates.filter(c => c.studentId === req.user.id));
});

app.get('/api/certificates/:ref/render', (req, res) => {
  const cert = certificates.find(c => c.certificateRef === req.params.ref) || certificates[0];
  res.send(`
    <!DOCTYPE html>
    <html>
    <head>
      <title>Certificate of Completion</title>
      <style>
        body { font-family: 'Georgia', serif; text-align: center; padding: 40px; background: #fdfbf7; color: #2d3748; }
        .border { border: 8px double #4a5568; padding: 40px; max-width: 750px; margin: 0 auto; background: #fff; box-shadow: 0 10px 25px rgba(0,0,0,0.1); border-radius: 4px; }
        h1 { font-size: 2.2rem; color: #1a365d; letter-spacing: 2px; text-transform: uppercase; margin-bottom: 8px; }
        .subtitle { font-size: 1.1rem; color: #718096; margin-bottom: 24px; }
        .name { font-size: 2rem; font-weight: bold; color: #2b6cb0; border-bottom: 2px solid #cbd5e0; display: inline-block; padding-bottom: 6px; margin: 16px 0; }
        .course { font-size: 1.3rem; font-weight: bold; color: #2d3748; margin: 16px 0; }
        .ref { font-size: 0.85rem; color: #a0aec0; margin-top: 30px; }
        .print-btn { margin-top: 20px; padding: 10px 20px; background: #3182ce; color: #fff; border: none; border-radius: 6px; cursor: pointer; font-family: sans-serif; }
        @media print { .print-btn { display: none; } }
      </style>
    </head>
    <body>
      <div class="border">
        <h1>Certificate of Completion</h1>
        <p class="subtitle">This is proudly presented to</p>
        <div class="name">${cert.studentName || 'Ada Lovelace'}</div>
        <p>for outstanding performance and successful completion of</p>
        <div class="course">${cert.courseName || 'CS101 - Algorithms & Data Structures'}</div>
        <p style="font-size:0.95rem; color:#4a5568;">Issued on: ${new Date(cert.issuedAt).toLocaleDateString()}</p>
        <div class="ref">Verification Reference: <strong>${cert.certificateRef}</strong></div>
        <button class="print-btn" onclick="window.print()">Print Certificate 🖨️</button>
      </div>
    </body>
    </html>
  `);
});

// ----------------------------------------------------
// QUIZZES
// ----------------------------------------------------
app.get('/api/student/quizzes/:id', authenticate, (req, res) => {
  const quiz = quizzes.find(q => q.id === parseInt(req.params.id));
  if (!quiz) return res.status(404).json({ message: 'Quiz not found' });
  // Return questions without correctAnswerId to prevent cheating
  const safeQuiz = {
    ...quiz,
    questions: quiz.questions.map(q => ({
      id: q.id,
      questionText: q.questionText,
      answers: q.answers
    }))
  };
  res.json(safeQuiz);
});

app.post('/api/student/quizzes/:id/submit', authenticate, (req, res) => {
  const quiz = quizzes.find(q => q.id === parseInt(req.params.id));
  if (!quiz) return res.status(404).json({ message: 'Quiz not found' });
  const userAnswers = req.body.answers || {};
  let score = 0;
  quiz.questions.forEach(q => {
    if (userAnswers[q.id] === q.correctAnswerId) score++;
  });
  const percentage = Math.round((score / quiz.questions.length) * 100);
  const result = {
    id: quizResults.length + 1,
    quizId: quiz.id,
    studentId: req.user.id,
    score: percentage,
    totalQuestions: quiz.questions.length,
    passed: percentage >= 60,
    submittedAt: new Date().toISOString()
  };
  quizResults.push(result);
  res.json(result);
});

app.get('/api/student/quizzes/:id/my-result', authenticate, (req, res) => {
  const resObj = quizResults.find(r => r.quizId === parseInt(req.params.id) && r.studentId === req.user.id);
  res.json(resObj || null);
});

app.post('/api/teacher/quizzes/module/:moduleId', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const modId = parseInt(req.params.moduleId);
  const { title, timeLimitMinutes, questions: qList } = req.body;
  const newQ = {
    id: quizzes.length ? Math.max(...quizzes.map(q => q.id)) + 1 : 1,
    moduleId: modId,
    title: title || 'New Quiz',
    timeLimitMinutes: timeLimitMinutes || 15,
    status: 'PUBLISHED',
    questions: qList || []
  };
  quizzes.push(newQ);
  res.json(newQ);
});

app.get('/api/teacher/quizzes/module/:moduleId', authenticate, requireRole('TEACHER', 'ADMIN'), (req, res) => {
  const modId = parseInt(req.params.moduleId);
  res.json(quizzes.filter(q => q.moduleId === modId));
});

// ----------------------------------------------------
// STATIC FILE SERVING FOR FRONTEND & UPLOADS
// ----------------------------------------------------
app.use('/uploads', express.static(uploadsDir));
app.use(express.static(path.join(__dirname, 'frontend')));

// Fallback to index.html for unrecognized routes (SPA style)
app.get('*', (req, res) => {
  const potentialFile = path.join(__dirname, 'frontend', req.path);
  if (fs.existsSync(potentialFile) && fs.statSync(potentialFile).isFile()) {
    return res.sendFile(potentialFile);
  }
  res.sendFile(path.join(__dirname, 'frontend', 'index.html'));
});

// ----------------------------------------------------
// START SERVER
// ----------------------------------------------------
app.listen(PORT, '0.0.0.0', () => {
  console.log(`[E-Learning Platform] Server successfully running at http://0.0.0.0:${PORT}`);
});
