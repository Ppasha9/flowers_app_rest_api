from rest_framework import viewsets, mixins

from core.models import Product
from product import serializers


class ProductViewSet(viewsets.GenericViewSet, mixins.ListModelMixin):
    """Manage products in database"""
    queryset = Product.objects.all()
    serializer_class = serializers.ProductSerializer

    def get_queryset(self):
        """Return all products in needed order"""
        return self.queryset.order_by('-name')
