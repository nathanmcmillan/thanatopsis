package com.gameinbucket.thanatopsis;

import com.gameinbucket.rendering.Box;
import com.gameinbucket.rendering.Thing;
import com.gameinbucket.rendering.VectorMath;

public class Baron extends Creature
{
	private static final int[] a_move = {0};
	private static final int[] a_death = {1};

	private boolean shadowed;
	private float fire_increment = 0;

	Baron(float x, float y, float z, int rx, int ry, int rz, GameHandler g, boolean shadow)
	{
		super(x, y + 0.6f, z, 0.7f, 3, 8, a_move, rx, ry, rz, 0.009f, g, 1);

		shadowed = shadow;
		present_action = ACTION_STANDING;
	}

	public void take_damage(Thing t, int d)
	{
		if (dead)
			return;

		health -= d;
		
		float angle_to_damager = angle_to_thing(t);
		
		view_v[0] -= Math.sin(angle_to_damager) * 0.3f;
		view_v[2] += Math.cos(angle_to_damager) * 0.3f;

		if (health <= 0)
		{
			dead = true;

			set_animation(a_death, 0);
			game.audio.play(R.raw.a_roar, 6.0f / (float)Math.sqrt(distance_to_thing(game.view_current)), angle_to_thing(game.view_current) - VectorMath.deg_to_rad(game.view_current.view_r[1]) + VectorMath.rad_180);
		}

		if (shadowed) game.dmg_engine.activate(view_p[0], view_p[1] + 0.7f, view_p[2], angle_to_thing(t), 0.0f, 0.0f, 0.0f, 0.5f);
		else game.dmg_engine.activate(view_p[0], view_p[1] + 0.7f, view_p[2], angle_to_thing(t), 0.0f, 0.5f, 0.25f, 1.0f);
	}

	protected void update_input()
	{
		if (dead)
		{
			if (view_p[1] > game.world.scale)
				view_p[1] -= 0.1f;
			else if (view_v[0] == 0 && view_v[2] == 0)
				remove = true;

			return;
		}

		float angle_to_protagonist = angle_to_thing(game.protagonist);

		if (distance_to_thing(game.protagonist) <= 90)
		{
			fire_increment++;
			
			if (fire_increment == 40)
			{
				fire_increment = 0;
				game.audio.play(R.raw.a_teleport, 6.0f / (float)Math.sqrt(distance_to_thing(game.view_current)), angle_to_thing(game.view_current) - VectorMath.deg_to_rad(game.view_current.view_r[1]) + VectorMath.rad_180);
				game.add_fire_ball(view_p[0], view_p[1], view_p[2], angle_to_protagonist, this);
			}
			
			view_v[0] -= Math.sin(angle_to_protagonist) * pace;
			view_v[2] += Math.cos(angle_to_protagonist) * pace;
		}
		else
		{
			view_v[0] += Math.sin(angle_to_protagonist) * pace;
			view_v[2] -= Math.cos(angle_to_protagonist) * pace;
		}
	}

	protected void update_position()
	{
		view_v[0] *= game.friction;
		view_v[2] *= game.friction;

		if (Math.abs(view_v[0]) < 0.001f) view_v[0] = 0;
		if (Math.abs(view_v[2]) < 0.001f) view_v[2] = 0;

		//object collision
		for (int i = 0; i < game.collidable.size(); i++)
		{
			Thing s = game.collidable.get(i);

			if (s == this)
				continue;

			boolean hit = false;

			if (collision_with_thing_x(s, view_p[0] + view_v[0]))
				hit = true;

			if (collision_with_thing_z(s, view_p[2] + view_v[2]))
				hit = true;

			if (!dead && hit && s instanceof Protagonist)
			{
				Protagonist p = (Protagonist)s;
				p.take_damage(this, 1);
			}
		}

		//box collision
		for (int i = 0; i < game.boxes.size(); i++)
		{
			Box b = game.boxes.get(i);

			collision_with_box_x(b, view_p[0] + view_v[0]);
			collision_with_box_z(b, view_p[2] + view_v[2]);
		}

		//world collision
		{
			cube_collision_x();
			view_p[0] += view_v[0];

			cube_collision_z();
			view_p[2] += view_v[2];
		}
	}

	protected void update_animation()
	{
		if (dead)
		{
			if (present_action != ACTION_DEAD)
			{
				next_frame();

				if (is_animation_last())
					present_action = ACTION_DEAD;
			}
		}
		else if (Math.abs(view_v[0]) >= 0.001f || Math.abs(view_v[2]) >= 0.001f)
		{
			present_action = ACTION_RUNNING;
			next_frame();
		}
		else if (present_action != ACTION_STANDING)
		{
			present_action = ACTION_STANDING;
			set_animation(a_move, 0);
		}
	}

	public float[] image()
	{
		if (shadowed) return VectorMath.create_sprite_alpha(view_p[0], view_p[1], view_p[2], game.rot_to_view, 0.7f, 1.4f, VectorMath.texture_8, tex0, tex1, tex2, tex3, 0.0f, 0.5f);
		else return VectorMath.create_sprite(view_p[0], view_p[1], view_p[2], game.rot_to_view, 0.7f, 1.4f, VectorMath.texture_8, tex0, tex1, tex2, tex3);
	}
}
