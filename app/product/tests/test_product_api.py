from django.contrib.auth import get_user_model
from django.urls import reverse
from django.test import TestCase

from rest_framework import status
from rest_framework.test import APIClient

from core.models import Product

from product.serializers import ProductSerializer


PRODUCTS_URL = reverse('product:product-list')


class PublicProductAPITests(TestCase):
    """Test the puclicly available products API"""

    def setUp(self):
        self.client = APIClient()

    def test_retrieve_all_products(self):
        """Test retrieving all products from database"""
        Product.objects.create(name='Flower1', description='descr1')
        Product.objects.create(name='Flower2', description='descr2')

        resp = self.client.get(PRODUCTS_URL)

        products = Product.objects.all().order_by('-name')
        serializer = ProductSerializer(products, many=True)

        self.assertEqual(resp.status_code, status.HTTP_200_OK)
        self.assertEqual(resp.data, serializer.data)


class PrivateProductAPITests(TestCase):
    """Test the privatly availbale products API"""

    def setUp(self):
        self.superuser = get_user_model().objects.create_superuser(
            email='testsuperusermail@gmail.com',
            password='testsuperpass'
        )
        self.client = APIClient()
        self.client.force_authenticate(user=self.superuser)

    def test_create_new_product_need_authentication(self):
        """Test that creating new product needs authentication"""
        payload = {'name': 'newproduct', 'description': 'newdescr'}
        client2 = APIClient()
        resp = client2.post(PRODUCTS_URL, payload)

        self.assertEqual(resp.status_code, status.HTTP_401_UNAUTHORIZED)

    def test_create_new_product_need_superuser(self):
        """Test that creating new product needs authentication"""
        nonsuper_user = get_user_model().objects.create_user(
            email='testemail@gmail.com',
            password='testpasstest'
        )
        client2 = APIClient()
        client2.force_authenticate(user=nonsuper_user)

        payload = {'name': 'newproduct', 'description': 'newdescr'}
        resp = client2.post(PRODUCTS_URL, payload)

        self.assertEqual(resp.status_code, status.HTTP_403_FORBIDDEN)

    def test_create_new_product_successful(self):
        """Test that creating new product by superuser is working correctly"""
        payload = {'name': 'newproduct2', 'description': 'newdescr2'}
        resp = self.client.post(PRODUCTS_URL, payload)

        exists = Product.objects.filter(
            name=payload['name'],
            description=payload['description']
        ).exists()

        self.assertEqual(resp.status_code, status.HTTP_201_CREATED)
        self.assertTrue(exists)

    def test_create_new_product_invalid(self):
        """Test creating new product with invalid payload"""
        payload = {'name': '', 'description': ''}
        resp = self.client.post(PRODUCTS_URL, payload)

        self.assertEqual(resp.status_code, status.HTTP_400_BAD_REQUEST)
