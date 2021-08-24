"""
The server config file.
"""


from flask import Flask
import os
app = Flask(__name__)

baseDir = os.path.abspath(os.path.dirname(__file__))

# SQLAlchemy config
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
# passphrase for encryption
app.config["password"] = "123456789"
# the address of the database file
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///' + os.path.join(baseDir, 'HIS.db')
app.config["baseDir"] = baseDir