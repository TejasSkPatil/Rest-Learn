# Phase 2 - Role-based Endpoint Security - Postman Test Cases

## Authentication
- Login as a user to get a JWT token
- Use the token in the Authorization header: `Bearer <jwt-token>`

## Test Cases

### 1. Student Role Tests
| Test Case | Endpoint | Token | Expected Status | Expected Response |
|-----------|----------|-------|-----------------|-------------------|
| S1 | `GET /api/student/ping` | Student token | 200 | `Email: student@university.edu, Role: STUDENT` |
| S2 | `GET /api/admin/ping` | Student token | 403 | Forbidden - student cannot access admin endpoints |
| S3 | `GET /api/teacher/ping` | Student token | 403 | Forbidden - student cannot access teacher endpoints |
| S4 | `GET /api/users/register` | Student token | 200 | Public endpoint - works |
| S5 | `GET /api/users/login` | Student token | 200 | Public endpoint - works |

### 2. Teacher Role Tests
| Test Case | Endpoint | Token | Expected Status | Expected Response |
|-----------|----------|-------|-----------------|-------------------|
| T1 | `GET /api/teacher/ping` | Teacher token | 200 | `Email: teacher@university.edu, Role: TEACHER` |
| T2 | `GET /api/admin/ping` | Teacher token | 403 | Forbidden - teacher cannot access admin endpoints |
| T3 | `GET /api/student/ping` | Teacher token | 403 | Forbidden - teacher cannot access student endpoints |
| T4 | `GET /api/users/register` | Teacher token | 200 | Public endpoint - works |
| T5 | `GET /api/users/login` | Teacher token | 200 | Public endpoint - works |

### 3. Admin Role Tests
| Test Case | Endpoint | Token | Expected Status | Expected Response |
|-----------|----------|-------|-----------------|-------------------|
| A1 | `GET /api/admin/ping` | Admin token | 200 | `Email: admin@university.edu, Role: ADMIN` |
| A2 | `GET /api/teacher/ping` | Admin token | 200 | `Email: admin@university.edu, Role: ADMIN` |
| A3 | `GET /api/student/ping` | Admin token | 200 | `Email: admin@university.edu, Role: ADMIN` |
| A4 | `GET /api/users/register` | Admin token | 200 | Public endpoint - works |
| A5 | `GET /api/users/login` | Admin token | 200 | Public endpoint - works |

### 4. Unauthenticated Tests
| Test Case | Endpoint | Token | Expected Status |
|-----------|----------|-------|-----------------|
| U1 | `GET /api/admin/ping` | No token | 401 Unauthorized |
| U2 | `GET /api/teacher/ping` | No token | 401 Unauthorized |
| U3 | `GET /api/student/ping` | No token | 401 Unauthorized |
| U4 | `GET /api/users/register` | No token | 200 Public endpoint |
| U5 | `GET /api/users/login` | No token | 200 Public endpoint |

### 5. General /api/** Tests (valid login required)
| Test Case | Endpoint | Token | Expected Status |
|-----------|----------|-------|-----------------|
| G1 | `GET /api/test` | Any valid token | 200 |
| G2 | `GET /api/users/register` | Any token | 200 (public) |
| G3 | `GET /api/users/login` | Any token | 200 (public) |

## How to Verify
1. Register three users with different roles (ADMIN, TEACHER, STUDENT)
2. Login as each user to get their JWT tokens
3. Use Postman to send requests with the respective tokens
4. Verify that each role can only access its own endpoints and public endpoints