from django.test import TestCase
from django.contrib.auth import get_user_model

from core import models


class ModelTests(TestCase):
    def test_create_user_with_email_successful(self):
        """Test creating a new user with email is successful"""
        email = "test@gmail.com"
        password = "TestPass123"
        user = get_user_model().objects.create_user(email=email, password=password)

        self.assertEqual(user.email, email)
        # the password in encrypted
        self.assertTrue(user.check_password(password))

    def test_new_user_email_normalized(self):
        """Test the email for a new user is normalized"""
        email = 'test@GMAIL.COM'
        password = "TestPass123"
        user = get_user_model().objects.create_user(email, password)

        self.assertEqual(user.email, email.lower())

    def test_new_user_invalid_email(self):
        """Test creating user with no email raises error"""
        with self.assertRaises(ValueError):
            get_user_model().objects.create_user(None, '123')

    def test_create_new_superuser(self):
        """Test creating a new superuser"""
        user = get_user_model().objects.create_superuser('test@test.com', '123')

        self.assertTrue(user.is_superuser)
        self.assertTrue(user.is_staff)

    def test_product_str(self):
        """Test __str__ method for model Product"""
        new_product = models.Product.objects.create(
            name='test product',
            description='test product for testing string repsresentation'
        )

        self.assertEqual(str(new_product), "'{}': {}".format(new_product.name, new_product.description))
