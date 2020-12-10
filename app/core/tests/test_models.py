from django.test import TestCase
from django.contrib.auth import get_user_model

from datetime import date

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

    def test_cart_str(self):
        """Test __str__ method of cart - test, that cart's user can be gotten by `.user` variable of Cart class"""
        user_name = 'TestUserName'
        new_user = get_user_model().objects.create_user(
            email='test@test.com',
            password='123',
            name=user_name)

        full_price = 30.59
        new_cart = models.Cart.objects.create(user=new_user, full_price=full_price)
        
        self.assertEqual(str(new_cart), f"Cart for user {user_name}, full price is {full_price}")

    def test_cart_to_products_relation(self):
        """Test some functions for cart-products relation"""
        new_user = get_user_model().objects.create_user(
            email='test@test.com',
            password='123',
            name='user_name')

        full_price = 30.59
        new_cart = models.Cart.objects.create(user=new_user, full_price=full_price)
        new_cart.products.create(name='Product1', description='Descr1', price=30.0, add_date=date.today())

        self.assertEqual(models.Product.objects.count(), 1)

        added_product = models.Product.objects.get(name='Product1')
        self.assertIsNotNone(added_product)

        self.assertEqual(added_product.description, 'Descr1')



