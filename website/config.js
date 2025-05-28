window.config =
{
    "team_name": "Sample Team",
    "team_number": "00000",
    "robot_devices": {
        "icons": {
            "dcmotor": "motor.svg",
            "dcmotorex": "motor.svg",
	    "dcmotorimpl": "motor.svg",
	    "dcmotorimplex": "motor.svg",
            "servo": "servo.svg",
	    "servoimpl": "servo.svg",
	    "servoimplex": "servo.svg",
            "unknown": "unknown.svg"
        },
        "icons_css": {
            "servo": "width: 105%; height: auto;"
        }
    },
    "modules": ["mission_control", "robot_devices", "telemetry", "file_manager", "camera_feed"],
    "apiEndpoints": {
        "allMethods": "/robot/all_methods",
		"allFields": "/robot/all_fields",
        "callMethod": "/robot/call_method",
        "deviceInfo": "/robot/device_info",
        "telemetry": "/robot/telemetry",
        "cameraFeed": "/robot/camera_feed",
        "fileListing": "/file/listing"
    },
    "missionControl": {
        "modules": ["code_editor", "code_buttons"]
    },
    "fileManager": {
        "fileEndpointPath": "/file",
        "editableFiles": [ "txt", "log" ]
    }
};