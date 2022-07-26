#version 300 es

/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const int MAX_JOINTS = 50;//max joints allowed in a skeleton
const int MAX_WEIGHTS = 3;//max number of joints that can affect a vertex

uniform mat4 u_ModelView;
uniform mat4 u_ModelViewProjection;
uniform mat4 jointTransforms[MAX_JOINTS];

layout(location = 0) in vec4 a_Position;
layout(location = 1) in vec2 a_TexCoord;
layout(location = 2) in vec3 a_Normal;
layout(location = 3) in ivec3 a_jointIndices;
layout(location = 4) in vec3 a_weights;

out vec3 v_ViewPosition;
out vec3 v_ViewNormal;
out vec2 v_TexCoord;

void main() {

//    for(int i=0;i<MAX_WEIGHTS;i++){
//        mat4 jointTransform = jointTransforms[in_jointIndices[i]];
//        vec4 posePosition = jointTransform * vec4(in_position, 1.0);
//        totalLocalPos += posePosition * in_weights[i];
//
//        vec4 worldNormal = jointTransform * vec4(in_normal, 0.0);
//        totalNormal += worldNormal * in_weights[i];
//    }


    v_ViewPosition = (u_ModelView * a_Position).xyz;
    v_ViewNormal = normalize((u_ModelView * vec4(a_Normal, 0.0)).xyz);
    v_TexCoord = a_TexCoord;
    gl_Position = u_ModelViewProjection * a_Position;
}

