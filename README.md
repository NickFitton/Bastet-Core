# Camera Backend

## Setup
No setup is needed thanks to [OpenPNP](https://github.com/openpnp/opencv#rationale) and their implementation of pre-compiled OpenCV.

## API
## User API
## User manipulation

### User can register with the system
#### Path
POST /v1/users

#### Request Body
```json
{
	"firstName": "John",
	"lastName": "Doe",
	"email": "j.doe@gmail.com",
	"Password": "password1234"
}
```

#### Response Status
On success:
* 201 Created
Other possible statuses:
* 400: Invalid request body
* 409: Email address already in use

#### Response Body
```json
{
  "id": "94b210e6-141a-4807-8e8b-1b516f164e72",
	"firstName": "John",
	"lastName": "Doe",
	"email": "j.doe@gmail.com"
}
```

### User can log in to the system
#### Path
POST /v1/login/user
#### Headers
Authorization header
#### Response Status
On success:
* 200: OK
Other possible statuses:
* 400: Invalid request
* 401: Authorization failed
#### Response Body
On success, the bearer token is returned in the Base64 format.
```json
{
  "data": "Y2MwNzkxNDUtYWMwOC00NDVlLTk1MWYtY2E5MTkyNzk5MTVl"
}
```

### User can update their information
#### Path
PUT /v1/users/{{userId}}
#### Headers
Bearer token
#### Request Body
```json
{
	"firstName": "John",
	"lastName": "Doe",
	"email": "j.doe@gmail.com",
	"Password": "password1234"
}
```
#### Response Status
On success:
* 202: Accepted
Other possible statuses:
* 400: Invalid request
* 401: Bad bearer token
* 403: Tried to update other users data
* 409: Email address in use

### User can delete their account
This removes their cameras, any camera data and the users account
#### Path
DELETE /v1/users/{{userId}}
#### Headers
Bearer token
#### Response Status
On success returns:
* 204: No Content
Other possible statuses:
* 400: Invalid request
* 401: Bad bearer token
* 403: Tried to delete other user

## Camera manipulation
### User can connect camera to account
#### Path
POST /v1/cameras/connect
#### Headers
Bearer token
#### Request Body
```json
{
	"cameraId": "875e506f-ce26-497c-bdc8-6d3dbe826f6c"
}
```
#### Response Status
On success:
* 201: Created
  * Camera connected but camera by ID is not currently registered to the system
* 202: Accepted
  * Camera connected and camera is already registered to the system
Other possible statuses:
* 400: Invalid request
* 401: Bad bearer token
* 409: Camera by id already registered to another user


### User can list cameras they have access to
#### Path
GET /v1/cameras
#### Headers
Bearer token

#### Response Status
On success:
* 200: OK
Other possible statuses:
* 401: Bad bearer token

#### Response Body
```json
{
	"Data": [
		{
			"id": "dc74dcea-8997-4960-9927-5b207ae059b3",
			"name": "front garden"
		}
	]
}
```

### Type statistics
Users can get statistics on the amount of tagged entities there have been between two times
#### Path
GET /v1/staticics/types
#### Headers
Bearer token
#### Query Parameters
**Camera**\
Specify cameras with the following:\
`camera=8a777a04-cfd6-455e-b153-ac8bd103e9b4`

Multiple cameras can be specified:\
`camera=8a777a04-cfd6-455e-b153-ac8bd103e9b4&camera=7b2717b9-7512-4c75-bf8f-168beb9ff175`

**Interval**
Can be one of the following:
* Hour
* Day
* Month
* Quarter
* Year

#### Response Status
On success:
* 200: OK
Other possible statuses:
* 400: Interval given invalid or camera ID not a valid UUID v4
* 401: Bad bearer token
* 404: Camera by id not found or not accessible by user
#### Response Body
```json
{
	"data": [
		{
			"time": "20080915T155300+0500",
			"stats": [
        {
          "type": "human",
          "frequency": 47
			  },
        {
          "type": "other",
          "frequency": 105
			  }
			]
		},
		{
			"time": "20080916T155300+0500",
			"stats": [
        {
          "type": "human",
          "frequency": 83
			  },
        {
          "type": "other",
          "frequency": 37
			  }
			]
		}
	]
}
```

### User can query new entity frequency
#### Path
GET /v1/statistics/frequency
#### Headers
Bearer token
#### Body
No body
#### Query parameters
**Camera**\
Specify cameras with the following:\
`camera=8a777a04-cfd6-455e-b153-ac8bd103e9b4`

Multiple cameras can be specified:\
`camera=8a777a04-cfd6-455e-b153-ac8bd103e9b4&camera=7b2717b9-7512-4c75-bf8f-168beb9ff175`

**Interval**
Can be one of the following:
* Hour
* Day
* Month
* Quarter
* Year

#### Response
On success:
* 200: OK

Other possible statuses:
* 400: Interval given invalid or camera ID not a valid UUID v4
* 401: Bad bearer token
* 404: Camera by id not found or not accessible by user

#### Response body
```json
{
  "data": [
    {
      "time": "20080915T155300+0500",
      "count": 150
    },
    {
      "time": "20080916T155300+0500",
      "count": 75
    }
  ]
}
```

### User can query motion data
Users can get the raw metadata about motion elements
#### Path
GET /v1/staticics/metdata
#### Headers
Bearer token
#### Query Parameters
**Camera**\
Specify cameras with the following:\
`camera=8a777a04-cfd6-455e-b153-ac8bd103e9b4`

Multiple cameras can be specified:\
`camera=8a777a04-cfd6-455e-b153-ac8bd103e9b4&camera=7b2717b9-7512-4c75-bf8f-168beb9ff175`

#### Response Status
On success:
* 200: OK
Other possible statuses:
* 400: Camera ID not a valid UUID v4
* 401: Bad bearer token
* 404: Camera by id not found or not accessible by user
#### Response Body
```json
{
	"data": [
		{
      "id": "366162c5-b64a-4fb2-a620-05e84550ffe6",
      "entryTime": "2018-12-19T13:11:23.894491Z",
      "exitTime": "2018-12-19T13:11:26.894491Z",
      "imageTime": "2018-12-19T13:11:24.894491Z",
      "fileExists": "",
      "imageEntities": ""
		}
	]
}
```

## Group Manipulation
### User can create a user group
User groups are used to let multiple users view one or more cameras.
#### Path
POST /v1/groups
#### Headers
Bearer token
#### Request Body
```json
{
	"name": "Family"
}
```
#### Response Status
On success:
* 201: Created
Other possible statuses:
* 400: Invalid request body
* 401: Bad bearer token

#### Response Body
```json
{
  "id": "94b210e6-141a-4807-8e8b-1b516f164e72",
	"name": "Family",
	"members": [
	  "366162c5-b64a-4fb2-a620-05e84550ffe6"
	],
	"cameras": []
}
```

### User can get a list of groups they are in
#### Path
GET /v1/groups
#### Headers
Bearer token
#### Response Status
On success:
* 200: OK
Other possible statuses:
* 401: Bad bearer token

#### Response Body
A list of the group ID's is returned to the user
```json
{
  "data": [
    "94b210e6-141a-4807-8e8b-1b516f164e72"
  ]
}
```

### User can get group information
#### Path
GET /v1/groups/{{groupId}}
#### Headers
Bearer token
#### Response Status
On success:
* 200: OK
Other possible statuses:
* 400: Invalid groupId
* 401: Bad bearer token
* 404: User not part of a group by that ID

#### Response Body
```json
{
  "id": "94b210e6-141a-4807-8e8b-1b516f164e72",
	"name": "Family",
	"members": [
	  "366162c5-b64a-4fb2-a620-05e84550ffe6"
	],
	"cameras": []
}
```

### User can invite other users to groups they are in
User groups are used to let multiple users view one or more cameras.
#### Path
POST /v1/groups/{{groupId}}/invite
#### Headers
Bearer token
#### Request Body
```json
{
  "addresses": [
    "j.doe@gmail.com",
    "janedoe@ntlworld.com"
  ]
}
```
#### Response Status
On success:
* 204 No Content
  * All addresses were invited
* 202 Accepted
  * Some of the given addresses were not part of the system and have been sent invitations to join the system
Other possible statuses:
* 400: Invalid request body
* 401: Bad bearer token
* 413: Tried to invite too may people at once

### Group owner can remove other users from the group
The owner of a group is the only user that can remove other users in a group
#### Path
POST /v1/groups/{{groupId}}/remove/{{userId}}
#### Headers
Bearer token
#### Response Status
On success:
* 202 Accepted
Other possible statuses:
* 400: Invalid request userId not valid UUID v4
* 401: Bad bearer token
* 404: User id not part of the selected group

### User can add camera to group they are in
If a user adds a camera to a group, it allows other users in the group to see data related to the camera, the camera must be owned by the user adding it.
#### Path
POST /v1/groups/{{groupId}}/cameras/{{cameraId}}
#### Headers
Bearer token
#### Response Status
On success:
* 202 Accepted
Other possible statuses:
* 400: Invalid request cameraId not valid UUID v4
* 401: Bad bearer token
* 404: Camera not found
* 409: Camera already part of the group

### User can remove cameras from a group they are in
If the user is the group owner or if the user owns the camera, then they can remove the camera from the group.
#### Path
DELETE /v1/groups/{{groupId}}/cameras/{{cameraId}}
#### Headers
Bearer token
#### Response Status
On success:
* 202 Accepted
Other possible statuses:
* 400: Invalid request cameraId not valid UUID v4
* 401: Bad bearer token
* 403: User does not have permission to delete the camera
* 404: Camera not found
* 409: Camera not part of the group

### Owner can set new owner of a group
Owner of a group can make another user in the group an owner (making the initial owner a regular user in the group).
#### Path
PATCH /v1/groups/{{groupId}}/owner/{{userId}}
#### Headers
Bearer token
#### Response Status
On success:
* 202 Accepted
Other possible statuses:
* 400: Invalid request userId not valid UUID v4
* 401: Bad bearer token
* 403: User does not have permission to set the owner of the group
* 404: User not found
* 409: User is already owner

## Camera API
### Camera registration
A camera can register itself to the system.\
The camera can give a predefined UUID or one can be assigned to it.
#### Path
POST /v1/cameras
#### Request Body
```json
{
  "id": "cfbc65fc-4f62-4fe3-bac3-9d5befc00941",
  "password": "password1234"
}
```
#### Response Status
On success:
* 201 Created
Other possible statuses:
* 400: Invalid request camera id not valid UUID v4
* 409: UUID is already in use
#### Response Body
```json
{
  "data": {
    "id": "cfbc65fc-4f62-4fe3-bac3-9d5befc00941",
    "createdAt": "2018-12-19T13:25:27.168Z",
    "updatedAt": "2018-12-19T13:25:27.168Z",
    "lastUpload": "2018-12-19T13:25:27.168Z"
  }
}
```
### Camera login
A camera can log in once it has registered.
#### Path
POST /v1/login/camera
#### Headers
Authorization header
#### Response Status
On success:
* 200: OK
Other possible statuses:
* 400: Invalid request
* 401: Authorization failed
#### Response Body
On success, the bearer token is returned in the Base64 format.
```json
{
  "data": "Y2MwNzkxNDUtYWMwOC00NDVlLTk1MWYtY2E5MTkyNzk5MTVl"
}
```

### Send motion data
The camera can post motion data.
#### Path
POST /v1/motion
#### Headers
Bearer token
#### Request Body
```json
{
  "entryTime": "2018-12-19T13:25:30Z",
  "exitTime": "2018-12-19T13:25:30Z",
  "imageTime": "2018-12-19T13:25:30Z"
}
```
#### Response Status
On success:
* 201: Created
Other possible statuses:
* 400: Invalid request
* 401: Authorization failed
#### Response Body
```json
{
  "data": {
    "id": "2031a594-ef27-4213-8796-cfe92c154e19",
    "entryTime": "2018-12-19T13:25:30Z",
    "exitTime": "2018-12-19T13:25:30Z",
    "imageTime": "2018-12-19T13:25:30Z",
    "createdAt": "2018-12-19T13:25:30.391Z",
    "updatedAt": "2018-12-19T13:25:30.391Z",
    "fileExists": false
  }
}
```

### Populate motion data with image
Camera can send image related to motion data
#### Path
PATCH /v1/motion/{{motionId}}
#### Headers
Bearer token
#### Request Body
Multipart:
Name: `file`
Value: image to upload
#### Response Status
On success:
* 202: Accepted
Other possible statuses:
* 400: Invalid request
* 401: Authorization failed
#### Response Body
```json
{
  "data": {
    "id": "2031a594-ef27-4213-8796-cfe92c154e19",
    "entryTime": "2018-12-19T13:25:30Z",
    "exitTime": "2018-12-19T13:25:30Z",
    "imageTime": "2018-12-19T13:25:30Z",
    "createdAt": "2018-12-19T13:25:30.391Z",
    "updatedAt": "2018-12-19T13:25:37.256Z",
    "fileExists": true
  }
}
```

