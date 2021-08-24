import distutils
import io
import traceback
from datetime import datetime, timedelta
from distutils.util import strtobool
from time import perf_counter

import eventlet
import matplotlib.pyplot
from matplotlib.backends.backend_agg import FigureCanvasAgg as FigureCanvas
from matplotlib.figure import Figure

import matplotlib.pyplot as plt
import matplotlib.ticker as ticker

eventlet.monkey_patch(thread=True, time=True)
from flask import Flask, request, render_template
import os
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import and_
from flask_socketio import SocketIO, join_room, emit

from model import db, Doctor, Patient, PatientThreshold, PatientHistory, ChatHistory, Supervise
from config import app
import json
from Crypto import Random
from Crypto.PublicKey import RSA
from Crypto.Cipher import PKCS1_OAEP, PKCS1_v1_5
import base64

# db.drop_all()
db.create_all()
#
# doc1 = Doctor(doctorID="1", doctorName="Dummy 1", doctorDepartment="Dummy", passwordHash="cc9b402d63549cb7e095efaaf2ed7399e09fb74bf746c08dd5ec722e44acfa41", senior = False)
# db.session.add(doc1)
# db.session.commit()
# doc2 = Doctor(doctorID="2", doctorName="Dummy 2", doctorDepartment="Dummy", passwordHash="cc9b402d63549cb7e095efaaf2ed7399e09fb74bf746c08dd5ec722e44acfa41", senior = False)
# db.session.add(doc2)
# doc3 = Doctor(doctorID="3",doctorName = "Senior 3",doctorDepartment="Dummy", passwordHash="cc9b402d63549cb7e095efaaf2ed7399e09fb74bf746c08dd5ec722e44acfa41", senior =True)
# db.session.add(doc3)
# sup1 = Supervise(seniorID="3", juniorID="1")
# db.session.add(sup1)
# sup2 = Supervise(seniorID="3", juniorID="2")
# db.session.add(sup2)
# db.session.commit()


socketio = SocketIO(app, async_mode="eventlet")
docSid = {}
docSid2 = {}
patSid = {}
patSid2 = {}
boardSid = []
patPhoneDict = {}
docPhoneDict = {}
startTime = perf_counter()
with open("dbSize.csv", "w")as f:
    f.write(str(startTime) + "," +str(os.path.getsize(os.path.abspath('HIS.db'))) + "\n")
with open("dbCount.csv", "w") as f:
    f.write(str(startTime) + "," + str(PatientHistory.query.count()) + "\n")
with open("statisticTime.csv", "w") as f:
    f.write("")


def decryptString(encryptedString):
    en = base64.b64decode(encryptedString)
    with open(os.path.join(app.config["baseDir"], "privateKey.bin")) as f:
        data = f.read()
    prk = RSA.importKey(data, app.config["password"])
    cipher = PKCS1_v1_5.new(prk)
    sentinel = None
    ret = cipher.decrypt(en, sentinel)
    return ret


def sampleDbSize():
    now = perf_counter()
    t = now - startTime
    with open("dbSize.csv", "a") as f:
        f.write(str(t) + "," + str(os.path.getsize(os.path.abspath('HIS.db'))) + "\n")

def sampleDbCount():
    now = perf_counter()
    t = now-startTime
    with open("dbCount.csv","a") as f:
        f.write(str(t) + "," + str(PatientHistory.query.count()) + "\n")


@app.before_first_request
def generate_keys():
    randomGenerator = Random.new().read
    rsa = RSA.generate(2048, randomGenerator)
    encryptedKey = rsa.exportKey(passphrase=app.config["password"], pkcs=8,
                                 protection="PBKDF2WithHMAC-SHA1AndAES256-CBC")
    with open(os.path.join(app.config["baseDir"], "privateKey.bin"), "wb") as f:
        f.write(encryptedKey)
    with open(os.path.join(app.config["baseDir"], "publicKey.pem"), "wb") as f:
        f.write(rsa.public_key().exportKey())


@app.route('/')
def hello_world():
    return 'Hello World!'


@app.route("/getPBK")
def getPBK():
    with open(os.path.join(app.config["baseDir"], "publicKey.pem")) as f:
        data = f.read()
    return data, 200


@app.route("/docLogin", methods=["POST"])
def docLogin():
    if request.method == "POST":
        try:
            encryptedLoginDetail = request.json["value"]
            try:
                loginDetail = decryptString(encryptedLoginDetail)
                loginJson = json.loads(loginDetail)
                # print(loginJson)
                doctorID = loginJson["doctorID"]
                passwordHash = loginJson["passwordHash"]
                phone = loginJson["phone"]
                try:
                    doctor = Doctor.query.filter_by(doctorID=doctorID, passwordHash=passwordHash).first()
                    if doctor is None:
                        return "", 404
                    else:
                        if doctor.senior:
                            res = json.loads('{"senior":""}')
                        else:
                            sup = doctor.supervisedBy
                            print(sup[0].seniorID)
                            param = {"senior": sup[0].seniorID}
                            res = json.loads(json.dumps(param))
                        docPhoneDict[doctorID] = phone
                        print(docPhoneDict)
                        print(res)
                        return res, 200
                except Exception as e:
                    print(str(e))
                    return "", 500
            except Exception as e:
                print(str(e))
                return "", 400
        except Exception as e:
            print(str(e))
            return "", 400
    else:
        return "", 405


@app.route("/reqPatientList", methods=["POST"])
def reqPatientList():
    if request.method == "POST":
        try:
            docID = request.json["doctorID"]
            doc = Doctor.query.filter_by(doctorID=docID).first()
            if not doc.senior:
                patients = Patient.query.filter_by(doctorID=docID).all()
                patientIDParams = []
                patientsParams = {}
                for patient in patients:
                    patientIDParams.append(patient.patientID)
                    patientParam = {"patientName": patient.patientName, "patientTag": patient.patientTag,
                                    "doctorID": patient.doctorID}
                    patientsParams[str(patient.patientID)] = patientParam
                patientsParams["patientIDList"] = patientIDParams
                # print(patientsParams)
                resJson = json.loads(json.dumps(patientsParams))
                return resJson, 200
            else:
                sup = doc.supervise
                patientIDParams = []
                patientsParams = {}
                print(sup)

                for supervise in sup:
                    patients = Doctor.query.filter_by(doctorID=supervise.juniorID).first().patient
                    for patient in patients:
                        patientIDParams.append(patient.patientID)
                        patientParam = {"patientName": patient.patientName, "patientTag": patient.patientTag,
                                        "doctorID": patient.doctorID}
                        patientsParams[str(patient.patientID)] = patientParam

                patientsS = doc.patient
                for patient in patientsS:
                    patientIDParams.append(patient.patientID)
                    patientParam = {"patientName": patient.patientName, "patientTag": patient.patientTag,
                                    "doctorID": patient.doctorID}
                    patientsParams[str(patient.patientID)] = patientParam

                patientsParams["patientIDList"] = patientIDParams
                resJson = json.loads(json.dumps(patientsParams))
                return resJson, 200
        except Exception as e:
            print(str(e))
            return "", 400


@app.route("/patientLogin", methods=["POST"])
def patientLogin():
    if request.method == "POST":
        try:
            patientID = request.json["patientID"]
            phone = request.json["phone"]
            # print(patientID)
            try:
                patient = Patient.query.filter_by(patientID=patientID).first()

                if patient is None:
                    return "", 404
                else:
                    patPhoneDict[patientID] = phone
                    patientInfo = {"id": patient.patientID, "name": patient.patientName, "tag": patient.patientTag,
                                   "docID": patient.doctorID, "senior": patient.seniorID}
                    print(patientInfo)
                    print(patPhoneDict)
                    resJson = json.loads(json.dumps(patientInfo))
                    return resJson, 200

            except Exception as e:
                print(str(e))
                return "", 500
        except Exception as e:
            print(str(e))
            return "", 400
    else:
        return "", 405


@app.route("/regPatient", methods=["POST"])
def regPatient():
    if request.method == "POST":
        try:
            patientName = request.json["patientName"]
            patientTag = request.json["patientTag"]
            doctorID = request.json["doctorID"]
            isSenior = bool(strtobool(request.json["isSenior"]))
            senior = None
            print(isSenior)
            if not isSenior:
                senior = request.json["senior"]
            heartrateNormalUpper = request.json["heartrateNormalUpper"]
            heartrateNormalLower = request.json["heartrateNormalLower"]
            heartrateOrangeUpper = request.json["heartrateOrangeUpper"]
            heartrateOrangeLower = request.json["heartrateOrangeLower"]

            spo2NormalUpper = request.json["spo2NormalUpper"]
            spo2NormalLower = request.json["spo2NormalLower"]
            spo2OrangeUpper = request.json["spo2OrangeUpper"]
            spo2OrangeLower = request.json["spo2OrangeLower"]
            try:
                patient = Patient(patientName=patientName, patientTag=patientTag, doctorID=doctorID, seniorID=senior)
                db.session.add(patient)
                db.session.commit()

                patientThreshold = PatientThreshold(patientID=patient.patientID,
                                                    heartrateNormalUpper=heartrateNormalUpper,
                                                    heartrateNormalLower=heartrateNormalLower,
                                                    heartrateOrangeUpper=heartrateOrangeUpper,
                                                    heartrateOrangeLower=heartrateOrangeLower,
                                                    spo2NormalUpper=spo2NormalUpper, spo2NormalLower=spo2NormalLower,
                                                    spo2OrangeUpper=spo2OrangeUpper, spo2OrangeLower=spo2OrangeLower
                                                    )
                db.session.add(patientThreshold)
                db.session.commit()

                responseParam = {"patientID": patient.patientID}
                responseParamJson = json.loads(json.dumps(responseParam))
                return responseParamJson, 200
            except Exception as e:
                print(str(e))
                return "", 500
        except Exception as e:
            print(str(e))
            return "", 400
    else:
        return "", 405


@app.route("/getPatientStatistic", methods=["POST"])
def getPatientStatistic():
    startT1 = perf_counter()
    try:
        id = int(request.json["id"])
        now = datetime.now()
        targetTime = now - timedelta(days=1)
        history = PatientHistory.query.filter(and_(PatientHistory.time >= targetTime, PatientHistory.patientID == id)).all()
        count = PatientHistory.query.filter(and_(PatientHistory.time >= targetTime, PatientHistory.patientID == id)).count()

        hrHighest = 0.0
        hrHighestTime = datetime.now()
        hrLowest = 999.0
        hrLowestTime = datetime.now()
        hrSum = 0.0

        spSum = 0.0
        spHighest = 0.0
        spLowest = 999.0
        spHighestTime = datetime.now()
        spLowestTime = datetime.now()

        normalSum = 0
        orangeSum = 0
        redSum = 0
        print(count)
        normalAverage = 0.0
        orangeAverage = 0.0
        redAverage = 0.0
        params = {"normal": 0.0, "orange": 0.0, "red": 0.0,
                  "hrAverage": 0.0, "hrHighest": 0.0,
                  "hrHighestTime": str(hrHighestTime), "hrLowest": 0.0,
                  "hrLowestTime": str(hrLowestTime),
                  "spAverage": 0.0, "spHighest": 0.0,
                  "spHighestTime": str(spHighestTime), "spLowest": 0.0,
                  "spLowestTime": str(spLowestTime)}
        if count > 0 :
            for entry in history:
                hrSum += int(entry.heartrate)
                spSum += int(entry.spo2)

                if entry.category == "Normal":
                    normalSum += 1
                elif entry.category == "Orange":
                    orangeSum += 1
                elif entry.category == "Red":
                    redSum += 1

                if entry.heartrate >= hrHighest:
                    hrHighest = int(entry.heartrate)
                    hrHighestTime = entry.time

                if entry.heartrate <= hrLowest:
                    hrLowest = int(entry.heartrate)
                    hrLowestTime = entry.time

                if entry.spo2 >= spHighest:
                    spHighest = entry.spo2
                    spHighestTime = entry.time

                if entry.spo2 <= spLowest:
                    spLowest = entry.spo2
                    spLowestTime = entry.time

                normalAverage = str(round(normalSum / count * 100, 2)) + "%"
                orangeAverage = str(round(orangeSum / count * 100, 2)) + "%"
                redAverage = str(round(redSum / count * 100, 2)) + "%"

                params = {"normal": normalAverage, "orange": orangeAverage, "red": redAverage,
                          "hrAverage": str(round(hrSum / count, 2)), "hrHighest": str(hrHighest),
                          "hrHighestTime": str(hrHighestTime), "hrLowest": str(hrLowest),
                          "hrLowestTime": str(hrLowestTime),
                          "spAverage": str(round(spSum / count, 2)), "spHighest": str(spHighest),
                          "spHighestTime": str(spHighestTime), "spLowest": str(spLowest),
                          "spLowestTime": str(spLowestTime)}
        else:
            params = {"normal": 0.0, "orange": 0.0, "red": 0.0,
                      "hrAverage": 0.0, "hrHighest": 0.0,
                      "hrHighestTime": str(hrHighestTime), "hrLowest": 0.0,
                      "hrLowestTime": str(hrLowestTime),
                      "spAverage": 0.0, "spHighest": 0.0,
                      "spHighestTime": str(spHighestTime), "spLowest": 0.0,
                      "spLowestTime": str(spLowestTime)}

        paramJson = json.loads(json.dumps(params))
        endT1 = perf_counter()
        with open("statisticTime.csv", "a") as f:
            f.write(str(count) + "," + str(endT1 - startT1) + "\n")
        return paramJson, 200
    except Exception as e:
        traceback.print_exc()
        print(e)
        return "", 500

@app.route("/getPatientStatisticPic", methods=["POST"])
def getPatientStatisticPic():
    try:
        print(request.form)
        print(request.form)

        id = request.form["id"]
        name = ""
        tag = ""
        patient = Patient.query.filter_by(patientID=id).first()
        if patient is not None:
            name = patient.patientName
            tag = patient.patientTag
        now = datetime.now()
        targetTime = now - timedelta(days=1)
        history = PatientHistory.query.filter(PatientHistory.time >= targetTime).all()
        dates = []
        heartrates = []
        spo2s = []

        for entry in history:
            dates.append(str(entry.time))
            heartrates.append(entry.heartrate)
            spo2s.append(entry.spo2)



        fig = plt.figure(figsize=(10,10))



        hrAxis = fig.add_subplot(2,1,1)
        spo2Axis = fig.add_subplot(2,1,2)
        for ax in fig.axes:
            matplotlib.pyplot.sca(ax)
            plt.xticks(rotation = 45)

        hrAxis.set_title("Heartrate chart in 1 day")
        hrAxis.set_xlabel("Time")
        hrAxis.set_ylabel("Heartrate")
        hrAxis.plot(dates,heartrates)
        hrAxis.grid()
        spo2Axis.set_title("SpO2 chart in 1 day")
        spo2Axis.set_xlabel("Time")
        spo2Axis.set_ylabel("SpO2")
        spo2Axis.plot(dates, spo2s)
        spo2Axis.grid()

        hrAxis.xaxis.set_major_locator(ticker.MultipleLocator(30))
        spo2Axis.xaxis.set_major_locator(ticker.MultipleLocator(30))
        fig.tight_layout()
        pngPic = io.BytesIO()
        FigureCanvas(fig).print_png(pngPic)
        pngImageB64String = "data:image/png;base64,"
        pngImageB64String += base64.b64encode(pngPic.getvalue()).decode("utf-8")
        return render_template("patientHistoryPic.html", image=pngImageB64String, id=id, name = name, tag = tag)

    except Exception as e:
        traceback.print_exc()
        print(str(e))
        return "", 500

@socketio.on("connect")
def connect():
    print(f"connect {request.sid}")
    type = request.args.get("type")
    if type is None:
        raise ConnectionRefusedError
    print(type)

    if type == "doc":
        docID = request.args.get("docID")
        if docID is None:
            raise ConnectionRefusedError
        docSid[docID] = request.sid
        docSid2[request.sid] = docID
        patients = Patient.query.filter_by(doctorID=docID).all()
        param = {"phone": docPhoneDict[docID]}
        paramJson = json.loads(json.dumps(param))

        for p in patients:
            if str(p.patientID) in patSid.keys():
                destSid = patSid[str(p.patientID)]
                emit("docOnline", paramJson, room=destSid)

    elif type == "patient":
        patID = request.args.get("patID")
        if patID is None:
            raise ConnectionRefusedError
        patSid[patID] = request.sid
        patSid2[request.sid] = patID
        patient = Patient.query.filter_by(patientID=patID).first()
        if patient is not None:
            docID = str(patient.doctorID)
            if docID in docSid.keys():
                param = {"phone": docPhoneDict[docID]}
                paramJson = json.loads(json.dumps(param))
                emit("docOnline", paramJson, room=request.sid)
        else:
            raise ConnectionRefusedError


    elif type == "board":
        boardSid.append(request.sid)
        patient = Patient.query.all()
        pIDList = []
        res = {}
        for p in patient:
            pIDList.append(p.patientID)
            patientParam = {"patientName": p.patientName, "patientColor": "Black"}
            res[str(p.patientID)] = patientParam
        res["patientIDList"] = pIDList
        resJson = json.loads(json.dumps(res))
        print(resJson)
        emit("updatePatient", resJson, room=request.sid)


@socketio.on("disconnect")
def disconnect():
    print(f"disconnect {request.sid}")

    if request.sid in docSid2.keys():
        docID = docSid2[request.sid]
        if docID in docSid.keys():
            docSid.pop(docID)
        if request.sid in docSid2.keys():
            docSid2.pop(request.sid)
        if docID in docPhoneDict:
            docPhoneDict.pop(docID)
        patients = Patient.query.filter_by(doctorID=docID).all()
        for p in patients:
            if str(p.patientID) in patSid.keys():
                destSid = patSid[str(p.patientID)]
                emit("docOffline", room=destSid)


    elif request.sid in patSid2.keys():
        patID = patSid2[request.sid]

        if patID in patSid.keys():
            patSid.pop(patID)
        if request.sid in patSid2.keys():
            patSid2.pop(request.sid)
        if patID in patPhoneDict.keys():
            patPhoneDict.pop(patID)

        patient = Patient.query.filter_by(patientID=patID).first()
        if patient is not None:
            param = {"name": patient.patientName, "id": patID, "tag": patient.patientTag, "color": "Black",
                     "heartrate": "", "spo2": "", "phone": "", "doctorID": patient.doctorID, "allowReply": False}
            patientJson = json.loads(json.dumps(param))
            if str(patient.doctorID) in docSid.keys():
                destSid = docSid[str(patient.doctorID)]
                emit("patientMonitorUpdate", patientJson, room=destSid)
            if str(patient.seniorID) in docSid.keys():
                destSid = docSid[str(patient.seniorID)]
                emit("patientMonitorUpdate", patientJson, room=destSid)
            for destSid in boardSid:
                print(destSid)
                emit("patientOffline", patientJson, room=destSid)


@socketio.on("patientMonitorUpdate")
def patientMonitorUpdate(data):
    try:
        patientID = data["patientID"]
        patientName = data["patientName"]
        patientTag = data["patientTag"]
        doctorID = data["docID"]
        senior = data["senior"]
        try:
            heartrate = int(data["heartrate"])
            spo2 = int(data["spo2"])
            category = ""
            try:

                patientThreshold = PatientThreshold.query.filter_by(patientID=patientID).first()
                heartrateNormalUpper = int(patientThreshold.heartrateNormalUpper)
                heartrateNormalLower = int(patientThreshold.heartrateNormalLower)
                heartrateOrangeUpper = int(patientThreshold.heartrateOrangeUpper)
                heartrateOrangeLower = int(patientThreshold.heartrateOrangeLower)

                spo2NormalUpper = int(patientThreshold.spo2NormalUpper)
                spo2NormalLower = int(patientThreshold.spo2NormalLower)
                spo2OrangeUpper = int(patientThreshold.spo2OrangeUpper)
                spo2OrangeLower = int(patientThreshold.spo2OrangeLower)

                if heartrate > heartrateOrangeUpper or heartrate < heartrateOrangeLower or spo2 > spo2OrangeUpper or spo2 < spo2OrangeLower:
                    category = "Red"
                elif heartrate > heartrateNormalUpper or heartrate < heartrateNormalLower or spo2 > spo2NormalUpper or spo2 < spo2NormalLower:
                    category = "Orange"
                else:
                    category = "Normal"
            except Exception as e:
                print(str(e))
            patientHis = PatientHistory(patientID=patientID, heartrate=heartrate, spo2=spo2, category=category)
            db.session.add(patientHis)
            db.session.commit()

            patient = {"name": patientName, "id": patientID, "tag": patientTag, "color": category,
                       "heartrate": str(heartrate), "spo2": str(spo2), "doctorID": str(doctorID),
                       "phone": patPhoneDict[patientID]}

            if doctorID in docSid.keys():
                patient["allowReply"] = True
            else:
                patient["allowReply"] = False

            patientJson = json.loads(json.dumps(patient))

            if doctorID in docSid.keys():
                destSid = docSid[doctorID]
                emit("patientMonitorUpdate", patientJson, room=destSid)
            if senior != "null" and senior in docSid.keys() and senior != doctorID:
                seniorSid = docSid[senior]
                emit("patientMonitorUpdate", patientJson, room=seniorSid)

            pIDList = [patientID]
            patientParam = {"patientName": patientName, "patientColor": category}
            res = {patientID: patientParam, "patientIDList": pIDList}
            resJson = json.loads(json.dumps(res))

            for destSid in boardSid:
                print(resJson, destSid)
                emit("updatePatient", resJson, room=destSid)
        except Exception as e:
            print(str(e))
    except Exception as e:
        print(str(e))


@socketio.on("uploadChatMsg")
def uploadChatMsg(data):
    try:
        msg = data["msg"]
        fromMsg = data["from"]
        pID = data["patientID"]
        dID = data["doctorID"]
        sID = data["seniorID"]
        time = datetime.strptime(data["time"], "%Y/%m/%d %H:%M:%S")

        print(f"{msg}, {time}, {fromMsg}, {pID}, {dID},{sID}")
        newMsg = ChatHistory(patientID=pID, doctorID=dID, seniorID=sID, fromSide=fromMsg, message=msg, time=time)
        db.session.add(newMsg)
        db.session.commit()

        print(dID, sID)

        msgJson = json.loads(json.dumps(data))
        if fromMsg == "patient":
            print(sID in docSid.keys())
            if dID in docSid.keys():
                destSid = docSid[dID]
                emit("chatUpdate", msgJson, room=destSid)
            if sID in docSid.keys() and sID != dID:
                destSid = docSid[sID]
                emit("chatUpdate", msgJson, room=destSid)

        elif fromMsg == "doctor":
            if pID in patSid.keys():
                destSid = patSid[pID]
                emit("chatUpdate", msgJson, room=destSid)
            if sID in docSid.keys() and sID != dID:
                destSid = docSid[sID]
                emit("chatUpdate", msgJson, room=destSid)

        elif fromMsg == "senior":
            if pID in patSid.keys():
                destSid = patSid[pID]
                emit("chatUpdate", msgJson, room=destSid)
            if dID in docSid.keys() and sID != dID:
                destSid = docSid[dID]
                emit("chatUpdate", msgJson, room=destSid)
        emit("confirm", msgJson, room=request.sid)

    except Exception as e:
        print(str(e))


def backgroundTask():
    while True:
        socketio.sleep(1)
        sampleDbSize()
        sampleDbCount()

if __name__ == '__main__':
    # db.init_app(app)
    # app.run(host="0.0.0.0", port=6122, debug=True)
    # print(str(os.getpid()))
    socketio.start_background_task(target=backgroundTask)
    socketio.run(app, host="0.0.0.0", port=6122, debug=True, use_reloader=False)
