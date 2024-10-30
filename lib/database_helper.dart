// lib/database_helper.dart
import 'dart:async';
import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';

class DatabaseHelper {
  static final DatabaseHelper _instance = DatabaseHelper._internal();
  factory DatabaseHelper() => _instance;
  DatabaseHelper._internal();

  static Database? _database;

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  Future<Database> _initDatabase() async {
    String path = join(await getDatabasesPath(), 'user_data.db');
    return await openDatabase(
      path,
      onCreate: (db, version) {
        return db.execute(
          'CREATE TABLE user(phone TEXT)',
        );
      },
      version: 1,
    );
  }

  Future<void> insertUser(String phone) async {
    final db = await database;
    await db.insert(
      'user',
      {'phone': phone},
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  Future<String?> getUserPhone() async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query('user');
    if (maps.isNotEmpty) {
      return maps.first['phone'] as String;
    }
    return null;
  }
}
