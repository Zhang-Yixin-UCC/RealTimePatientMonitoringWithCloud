from flask import Flask
import os
app = Flask(__name__)

baseDir = os.path.abspath(os.path.dirname(__file__))

app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config["password"] = "123456789"
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///' + os.path.join(baseDir, 'HIS.db')
app.config["baseDir"] = baseDir