#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;

void main()
{
	vec2 shift = vec2(1.0/240.0, 0);
	float live = texture2D(u_texture, v_texCoords).r;
	
	float neighbours = 0.0;
	
	neighbours += texture2D(u_texture, v_texCoords + shift).r;
	neighbours += texture2D(u_texture, v_texCoords - shift).r;
	
	neighbours += texture2D(u_texture, v_texCoords + shift.yx).r;
	neighbours += texture2D(u_texture, v_texCoords - shift.yx).r;
	
	neighbours += texture2D(u_texture, v_texCoords + shift.xx).r;
	neighbours += texture2D(u_texture, v_texCoords - shift.xx).r;
	
	neighbours += texture2D(u_texture, v_texCoords + vec2(shift.x, -shift.x)).r;
	neighbours += texture2D(u_texture, v_texCoords - vec2(shift.x, -shift.x)).r;
	
	if((live == 1.0 && (neighbours == 3.0 || neighbours == 2.0)) || (live == 0.0 && neighbours == 3.0)){
		gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
	} else {
		gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
	}
}