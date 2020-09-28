from rest_framework.response import Response
from rest_framework import viewsets, mixins, status
from rest_framework.authentication import TokenAuthentication
from rest_framework.permissions import IsAdminUser
from rest_framework.decorators import action

from core.models import Product
from product import serializers


class _CustomProductPermission(IsAdminUser):
    protected_methods = ['POST',]

    def has_permission(self, request, view):
        if request.method in self.protected_methods:
            return super().has_permission(request, view)
        return True


class _CustomProductAuthentication(TokenAuthentication):
    protected_methods = ['POST',]

    def has_authentication(self, request, view):
        if request.method in self.protected_methods:
            return super().has_authentication(request, view)
        return True


class ProductViewSet(viewsets.GenericViewSet,
                     mixins.ListModelMixin,
                     mixins.CreateModelMixin):
    """Viewset for listing and creating products in database"""
    queryset = Product.objects.all()
    serializer_class = serializers.ProductSerializer
    permission_classes = (_CustomProductPermission,)
    authentication_classes = (_CustomProductAuthentication,)

    def get_queryset(self):
        """Return all products in needed order"""
        return self.queryset.order_by('-name')

    def perform_create(self, serializer):
        """Special method for overriding post request for creating new products"""
        serializer.save()
