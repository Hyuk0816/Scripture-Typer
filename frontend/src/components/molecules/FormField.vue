<script setup lang="ts">
import Label from '@/components/atoms/Label.vue'
import InputField from '@/components/atoms/InputField.vue'
import ErrorMessage from '@/components/atoms/ErrorMessage.vue'

defineProps<{
  modelValue: string
  label: string
  type?: string
  placeholder?: string
  error?: string
  required?: boolean
  disabled?: boolean
  id?: string
}>()

defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<template>
  <div>
    <Label :html-for="id" :required="required">{{ label }}</Label>
    <InputField
      :id="id"
      :model-value="modelValue"
      :type="type"
      :placeholder="placeholder"
      :error="!!error"
      :disabled="disabled"
      @update:model-value="$emit('update:modelValue', $event)"
    />
    <ErrorMessage :message="error" />
  </div>
</template>
