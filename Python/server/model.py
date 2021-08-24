"""
The data model.
These affect the schema of the database.

"""

from flask import Flask
from flask_sqlalchemy import SQLAlchemy
import os
from sqlalchemy import func
from config import app

db = SQLAlchemy(app, session_options={'expire_on_commit': False})


class Doctor(db.Model):
    _tablename_ = "doctor"
    doctorID = db.Column(db.Integer, primary_key=True, autoincrement=True, nullable=False)
    doctorName = db.Column(db.String(100), nullable=False)
    doctorDepartment = db.Column(db.String(100), nullable=False)
    passwordHash = db.Column(db.Text, nullable=False)
    senior = db.Column(db.Boolean, nullable=False)
    # chatHistory = db.relationship("ChatHistory", backref="doctor", lazy=True)

class Supervise(db.Model):
    _tablename_ = "supervise"
    __table_args__ = (
        db.PrimaryKeyConstraint("seniorID", "juniorID"),
        db.CheckConstraint("seniorID != juniorID")
    )
    seniorID = db.Column(db.Integer, db.ForeignKey(Doctor.doctorID), nullable=False)
    juniorID = db.Column(db.Integer, db.ForeignKey(Doctor.doctorID), nullable=False)
    seniorDoc= db.relationship("Doctor",foreign_keys = [seniorID], backref = "supervise")
    juniorDoc = db.relationship("Doctor", foreign_keys = [juniorID], backref ="supervisedBy")


class Patient(db.Model):
    _tablename_ = "patient"
    patientID = db.Column(db.Integer, primary_key=True, autoincrement=True, nullable=False)
    patientName = db.Column(db.String(100), nullable=False)
    patientTag = db.Column(db.String(200), nullable=True)
    doctorID = db.Column(db.Integer, db.ForeignKey(Doctor.doctorID), nullable=False)
    seniorID = db.Column(db.Integer, db.ForeignKey(Doctor.doctorID))
    doc = db.relationship("Doctor", foreign_keys=[doctorID], backref = "patient")
    senior = db.relationship("Doctor", foreign_keys=[seniorID], backref = "patientSupervised")
    # patientThreshold = db.relationship("PatientThreshold", backref="patient", lazy=True, uselist=False)
    # patientHistory = db.relationship("PatientHistory", backref="patient", lazy=True)
    # chatHistory = db.relationship("ChatHistory", backref="patient", lazy=True)


class PatientThreshold(db.Model):
    _tablename_ = "patientThreshold"
    patientID = db.Column(db.Integer, db.ForeignKey(Patient.patientID), primary_key=True, nullable=False)
    heartrateNormalUpper = db.Column(db.Float, nullable=False)
    heartrateNormalLower = db.Column(db.Float, nullable=False)
    heartrateOrangeUpper = db.Column(db.Float, nullable=False)
    heartrateOrangeLower = db.Column(db.Float, nullable=False)
    spo2NormalUpper = db.Column(db.Float, nullable=False)
    spo2NormalLower = db.Column(db.Float, nullable=False)
    spo2OrangeUpper = db.Column(db.Float, nullable=False)
    spo2OrangeLower = db.Column(db.Float, nullable=False)
    p = db.relationship("Patient", backref = "threshold")

class PatientHistory(db.Model):
    _tablename_ = "patientHistory"
    __table_args__ = (
        db.PrimaryKeyConstraint("patientID", "time"),
    )
    patientID = db.Column(db.Integer, db.ForeignKey(Patient.patientID), nullable=False)
    heartrate = db.Column(db.Float, nullable=False)
    spo2 = db.Column(db.Float, nullable=False)
    category = db.Column(db.String(10), nullable=False)
    time = db.Column(db.DateTime(timezone=True), server_default=func.now(), nullable=False)

class ChatHistory(db.Model):
    _tablename_ = "chatHistory"
    msgID = db.Column(db.Integer, primary_key=True, autoincrement=True)
    patientID = db.Column(db.Integer, db.ForeignKey(Patient.patientID), nullable=False)
    doctorID = db.Column(db.Integer, db.ForeignKey(Doctor.doctorID), nullable=False)
    seniorID = db.Column(db.Integer, db.ForeignKey(Doctor.doctorID))
    fromSide = db.Column(db.String(1), nullable=False)
    message = db.Column(db.Text, nullable=False)
    time = db.Column(db.DateTime(timezone=True), server_default=func.now(), nullable=False)
    doctor = db.relationship("Doctor", foreign_keys = [doctorID])
    senior = db.relationship("Doctor", foreign_keys = [seniorID])

