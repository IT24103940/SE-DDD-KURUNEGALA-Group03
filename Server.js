const express = require("express");
const bodyParser = require("body-parser");
const cors = require("cors");
const sql = require("mssql");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");

const app = express();
app.use(cors());
app.use(bodyParser.json());

// SQL Server Config
const dbConfig = {
    user: "YOUR_SQL_USERNAME",
    password: "YOUR_SQL_PASSWORD",
    server: "localhost",   // or your server name
    database: "UserAuthDB",
    options: {
        encrypt: false,      // true if using Azure SQL
        trustServerCertificate: true
    }
};

// Secret key for JWT
const JWT_SECRET = "mysecretkey123";

// Register endpoint
app.post("/register", async (req, res) => {
    try {
        const { username, password } = req.body;

        let pool = await sql.connect(dbConfig);
        let existing = await pool.request()
            .input("username", sql.NVarChar, username)
            .query("SELECT * FROM Users WHERE Username = @username");

        if (existing.recordset.length > 0) {
            return res.status(400).json({ message: "Username already exists" });
        }

        const hashedPassword = await bcrypt.hash(password, 10);

        await pool.request()
            .input("username", sql.NVarChar, username)
            .input("passwordHash", sql.NVarChar, hashedPassword)
            .query("INSERT INTO Users (Username, PasswordHash) VALUES (@username, @passwordHash)");

        res.json({ message: "Registration successful" });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: "Server error" });
    }
});

// Login endpoint
app.post("/login", async (req, res) => {
    try {
        const { username, password } = req.body;

        let pool = await sql.connect(dbConfig);
        let result = await pool.request()
            .input("username", sql.NVarChar, username)
            .query("SELECT * FROM Users WHERE Username = @username");

        if (result.recordset.length === 0) {
            return res.status(400).json({ message: "Invalid username or password" });
        }

        const user = result.recordset[0];
        const isMatch = await bcrypt.compare(password, user.PasswordHash);

        if (!isMatch) {
            return res.status(400).json({ message: "Invalid username or password" });
        }

        // Generate token
        const token = jwt.sign({ id: user.UserID, role: user.Role }, JWT_SECRET, { expiresIn: "1h" });

        res.json({ message: "Login successful", token, role: user.Role, username: user.Username });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: "Server error" });
    }
});

// Admin panel - list users
app.get("/users", async (req, res) => {
    try {
        let pool = await sql.connect(dbConfig);
        let result = await pool.request().query("SELECT UserID, Username, Role FROM Users");
        res.json(result.recordset);
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: "Server error" });
    }
});

// Start server
app.listen(5000, () => {
    console.log("Server running on http://localhost:5000");
});
