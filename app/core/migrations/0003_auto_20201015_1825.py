# Generated by Django 2.1.15 on 2020-10-15 15:25

import datetime
from django.conf import settings
from django.db import migrations, models
import django.db.models.deletion
from django.utils.timezone import utc


class Migration(migrations.Migration):

    dependencies = [
        ('core', '0002_product'),
    ]

    operations = [
        migrations.CreateModel(
            name='Cart',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('date', models.DateField(default=datetime.datetime(2020, 10, 15, 15, 25, 37, 316487, tzinfo=utc))),
                ('full_price', models.FloatField(default=0.0)),
            ],
        ),
        migrations.AddField(
            model_name='product',
            name='add_date',
            field=models.DateField(default=datetime.datetime(2020, 10, 15, 15, 25, 37, 315491, tzinfo=utc)),
        ),
        migrations.AddField(
            model_name='product',
            name='price',
            field=models.FloatField(default=0.0),
        ),
        migrations.AddField(
            model_name='user',
            name='register_date',
            field=models.DateField(default=datetime.datetime(2020, 10, 15, 15, 25, 37, 313495, tzinfo=utc)),
        ),
        migrations.AddField(
            model_name='user',
            name='sex',
            field=models.CharField(default='M', max_length=1),
        ),
        migrations.AlterField(
            model_name='product',
            name='description',
            field=models.TextField(),
        ),
        migrations.AlterField(
            model_name='product',
            name='name',
            field=models.CharField(max_length=255),
        ),
        migrations.AddField(
            model_name='cart',
            name='products',
            field=models.ManyToManyField(to='core.Product'),
        ),
        migrations.AddField(
            model_name='cart',
            name='user',
            field=models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to=settings.AUTH_USER_MODEL),
        ),
    ]
