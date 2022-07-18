#version 120

uniform sampler2D DiffuseSampler;
uniform sampler2D VanillaTexture;

varying vec2 texCoord;
varying vec2 oneTexel;

uniform vec2 InSize;

void main() {
    int r=1;
    float size=(2*r+1)*(2*r+1);
    float count=0;
    vec4 blurColor=vec4(0,0,0,0);
    for(float x=-r;x<=r;x++){
            for(float y=-r;y<=r;y++){
                vec4 color=texture2D(DiffuseSampler,texCoord+vec2(x/InSize.x,y/InSize.y));
                float s=sign(color.a);
                blurColor+=color*s+texture2D(VanillaTexture,texCoord+vec2(x/InSize.x,y/InSize.y))*(1-s);
                count+=s;
            }
    }
    //gl_FragColor = texture2D(DiffuseSampler,texCoord+vec2(5.0/InSize.x,5.0/InSize.y));
    blurColor=blurColor/size;
    blurColor.a*=(count/size);
    float dx=texCoord.x-0.5;
    float dy=texCoord.y-0.5;
    float flag=clamp(sqrt(dx*dx+dy*dy)/0.2,0,1);
    //flag=1;
    gl_FragColor=blurColor*flag+texture2D(DiffuseSampler,texCoord)*(1-flag);
}
