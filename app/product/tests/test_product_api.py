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
